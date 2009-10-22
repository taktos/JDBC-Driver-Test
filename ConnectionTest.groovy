import java.sql.*

// groovy -cp [driver.jar] ConnectionTest.groovy [driverClass] [url] [user] [password]
Class driverClass = Class.forName(args[0])
Driver driver = driverClass.newInstance()
println "${driver.class.name}-${driver.majorVersion}.${driver.minorVersion} [JDBC Compliant: ${driver.jdbcCompliant()}]"
DriverManager.registerDriver(driver)

class ConnectionPool {
	def pool = []
	def url
	def user
	def password
	def connectCount = 0
	def getPhysicalConnection() {
		def con = DriverManager.getConnection(url, user, password)
		con.setAutoCommit(false)
		connectCount++
		return con
	}

	def checkOut() {
		if (pool.size == 0) getPhysicalConnection()
		else pool.pop()
	}

	def checkIn(con) {
		this.pool.push(con)
	}
}

pool = new ConnectionPool(url:args[1], user:args[2], password:args[3])

pool.checkIn(pool.checkOut())
print 'Please restart the DB server within 30 seconds'
(1..30).each { Thread.sleep(1000); print '.'; }
println ""

for (i in 1..2) {
    def con = pool.checkOut()
    def ps
    try {
        ps = con.prepareStatement("insert into foo values (?)")
        ps.setInt(1, i)
    } catch (SQLException e) {
        continue // do not return a connection to pool
    }

    try {
        ps.executeUpdate()
    } catch (SQLException e) {
        try {
            // executeXXX throws not only connection error,
            // we must rollback this transaction.
            con.rollback()
        } catch (SQLException rollbackFailed) {
            continue // do not return a connection to pool
        }
    }
	pool.checkIn(con) // returns a connection to pool
}

assert pool.connectCount == 2

