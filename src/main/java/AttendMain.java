import com.avaje.ebean.EbeanServer;
import com.avaje.ebean.EbeanServerFactory;
import com.avaje.ebean.config.ServerConfig;
import model.Receipt;
import org.avaje.agentloader.AgentLoader;

import java.util.Date;

public class AttendMain {
    
    public static void main(String[] args) {
        
        if (!AgentLoader.loadAgentFromClasspath("avaje-ebeanorm-agent","debug=1;packages=model")) {
            System.out.println("ebean-agent not found in classpath - not dynamically loaded");
        }
        
//        setupEbean();
    
        AttendAPI api = new AttendAPI();
        api.deploy();
    }
    
    private static void setupEbean() {
        ServerConfig config = new ServerConfig();
        config.setName("mysql");
        config.loadFromProperties();
        
        config.addClass(Receipt.class);
        config.addPackage("model");
        
        config.getClasses().stream().forEach(a->System.out.println(a.getName()));
        
        EbeanServer server = EbeanServerFactory.create(config);
        
// Define DataSource parameters
//        DataSourceConfig postgresDb = new DataSourceConfig();
//        postgresDb.setDriver("org.postgresql.Driver");
//        postgresDb.setUsername("test");
//        postgresDb.setPassword("test");
//        postgresDb.setUrl("jdbc:postgresql://127.0.0.1:5432/test");
//        postgresDb.setHeartbeatSql("select count(*) from t_one");
//
//        config.setDataSourceConfig(postgresDb);
//
//// specify a JNDI DataSource
//// config.setDataSourceJndiName("someJndiDataSourceName");
//
//// set DDL options...
//        config.setDdlGenerate(true);
//        config.setDdlRun(true);
//
//        config.setDefaultServer(false);
//        config.setRegister(false);
//
//
//// automatically determine the DatabasePlatform
//// using the jdbc driver
//// config.setDatabasePlatform(new PostgresPlatform());
//
//// specify the entity classes (and listeners etc)
//// ... if these are not specified Ebean will search
//// ... the classpath looking for entity classes.
////        config.addClass(Order.class);
////        config.addClass(Customer.class);
//
//// create the EbeanServer instance
//        EbeanServer server = EbeanServerFactory.create(config);
    }
}
