import java.sql.*

// groovy -cp [driver.jar] ConnectionTest.groovy [driverClass] [url] [user] [password]
Class driverClass = Class.forName(args[0])
Driver driver = driverClass.newInstance()
println "${driver.class.name}-${driver.majorVersion}.${driver.minorVersion} [JDBC Compliant: ${driver.jdbcCompliant()}]"
DriverManager.registerDriver(driver)
Connection con = DriverManager.getConnection(args[1], args[2], args[3]);
con.setAutoCommit(false);

print 'Please shut down the DB server within 30 seconds'
(1..30).each { Thread.sleep(1000); print '.'; }
println ""

try {
    con.commit()
    println 'Connection#commit() throws NO exception!!'
} catch (SQLException e) {
    println "Connection#commit() throws exception. ${e.toString()}"
}

