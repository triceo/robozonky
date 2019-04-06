module com.github.robozonky.cli {
    requires google.api.services.drive.v3.rev153;
    requires google.api.services.sheets.v4.rev565;
    requires google.http.client;
    requires google.oauth.client;
    requires info.picocli;
    requires io.vavr;
    requires logback.classic;
    requires logback.core;
    requires org.apache.commons.io;
    requires org.apache.logging.log4j;
    requires com.github.robozonky.api;
    requires com.github.robozonky.common;
    requires com.github.robozonky.integration.stonky;

    exports com.github.robozonky.cli;
}
