package demos;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.ftpserver.FtpServer;

import static demos.FtpServerUtil.embeddedFtpServer;
import static org.apache.camel.LoggingLevel.WARN;
import static org.apache.camel.model.dataformat.BindyType.Csv;

public class Demo {

    public static void main(String args[]) throws Exception {

        FtpServer ftpServer = embeddedFtpServer(21000, Demo.class.getResource("/users.properties"));
        ftpServer.start();

        CamelContext context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            public void configure() {
                from("ftp:rider:secret@localhost:21000/data/inbox?noop=true" +
                        "&connectTimeout=5000&timeout=5000")
                        .unmarshal().bindy(Csv, "demos")
                        .log(WARN, "aggre ${in.header.CamelFileName}: ${body}")
                        .marshal().bindy(Csv, "demos")
                        .to("file:data/outbox")
                ;
            }
        });

        context.start();
        Thread.sleep(2000);

        context.stop();

        ftpServer.stop();
    }
}