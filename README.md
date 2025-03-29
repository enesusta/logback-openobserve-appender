# Logback Openobserve Appender

Send log events directly from Logback to OpenObserve. Logs are delivered asynchronously (i.e. not on the main thread, it actually runs on deamon thread like a lots of jvm threads do in background) so will not block execution of the program.
Note that the queue backlog can be bounded and messages _can_ be lost if **OpenObserve** is down and either the backlog queue is full or the producer program is trying to exit (it will retry up to a configured number of attempts, but will not block shutdown of the program beyond that).
For long-lived programs, this should not be a problem, as messages should be delivered eventually.

This software is dual-licensed under the EPL 1.0 and LGPL 2.1, which is identical to the [Logback License](http://logback.qos.ch/license.html) itself.

# Usage

Include slf4j and logback as usual (depending on this library will _not_ automatically pull them in).

In your `pom.xml` (or equivalent), add:

In your `logback.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration debug="true" scan="true">

    <property name="env" value="${SPRING_PROFILES_ACTIVE}" />
    <shutdownHook class="ch.qos.logback.core.hook.DelayingShutdownHook" />

    <appender name="OPEN_OBSERVE" class="com.github.enesusta.logback.openobserve.OpenObserveAppender">
        <url>http://localhost:5080/api/default/enesusta/_json</url>
        <includeMdc>true</includeMdc>
        <connectTimeout>30000</connectTimeout>
        <readTimeout>30000</readTimeout>
        <type>tester</type>
        <loggerName>openobserve-logger</loggerName>
        <errorLoggerName>openobserve-error-logger</errorLoggerName>
        <initialDelay>1</initialDelay>
        <delay>1</delay>
        <timeUnit>SECONDS</timeUnit>

        <headers>
            <header>
                <name>Content-Type</name>
                <value>application/json</value>
            </header>
            <header>
                <name>Authorization</name>
                <value>
                    <!-- your openobserve credentials converted in base64 -->
                </value>
            </header>
        </headers>

        <properties>
             <!-- please note that openObserveProperty was added for logback-1.3 compatibility -->
            <openObserveProperty>
                <name>host</name>
                <value>${HOSTNAME}</value>
                <allowEmpty>false</allowEmpty>
            </openObserveProperty>
            <openObserveProperty>
                <name>severity</name>
                <value>%level</value>
            </openObserveProperty>
            <openObserveProperty>
                <name>thread</name>
                <value>%thread</value>
            </openObserveProperty>
            <openObserveProperty>
                <name>stacktrace</name>
                <value>%ex</value>
            </openObserveProperty>
            <openObserveProperty>
                <name>root_cause</name>
                <value>%rootException</value>
            </openObserveProperty>
            <openObserveProperty>
                <name>logger</name>
                <value>%logger</value>
            </openObserveProperty>

        </properties>
    </appender>

    <root level="INFO">
        <appender-ref ref="OPEN_OBSERVE" />
    </root>


</configuration>
```

# Configuration Reference

- `url` (required): The URL to your OpenObserve bulk API endpoint
- `connectTimeout` (optional, default 30000): OpenObserve connect timeout (in ms)
- `readTimeout` (optional, default 30000): OpenObserve read timeout (in ms)
- `initialDelay` (optional, default 0): OpenObserve appender runs a scheduled job which publish logback events to OpenObserve server, this option will be used in mentioned scheduled job to designate initial delay (in TimeUnit that you specify)
- `delay` (optional, default 1): OpenObserve appender runs a scheduled job which publish logback events to OpenObserve server, this option will be used in mentioned scheduled job to designate delay (in TimeUnit that you specify)
- `timeUnit` (optional, default SECONDS): OpenObserve appender runs a scheduled job which publish logback events to OpenObserve server, this option will be used in mentioned scheduled job to designate frequency in your job
  - Available options for [TimeUnit]:
    - DAYS
    - HOURS
    - MINUTES
    - SECONDS
    - MILLISECONDS
    - MICROSECONDS
    - NANOSECONDS
- `includeCallerData` (optional, default false): If set to `true`, save the caller data (identical to the [AsyncAppender's includeCallerData](http://logback.qos.ch/manual/appenders.html#asyncIncludeCallerData))
- `errorsToStderr` (optional, default false): If set to `true`, any errors in communicating with OpenObserve will also be dumped to stderr (normally they are only reported to the internal Logback Status system, in order to prevent a feedback loop)
- `logsToStderr` (optional, default false): If set to `true`, dump the raw OpenObserve messages to stderr
- `loggerName` (optional): If set, raw ES-formatted log data will be sent to this logger
- `errorLoggerName` (optional): If set, any internal errors or problems will be logged to this logger
- `rawJsonMessage` (optional, default false): If set to `true`, the log message is interpreted as pre-formatted raw JSON message.
- `includeMdc` (optional, default false): If set to `true`, then all [MDC](http://www.slf4j.org/api/org/slf4j/MDC.html) values will be mapped to properties on the JSON payload.
- `objectSerialization` (optional): specifies whether to use POJO to JSON serialization

The field `message` are always sent and can not currently be configured. Additional fields can be sent by adding `<openObserveProperty>` elements to the `<properties>` set.

- `name` (required): Key to be used in the log event
- `value` (required): Text string to be sent. Internally, the value is populated using a Logback PatternLayout, so all [Conversion Words](http://logback.qos.ch/manual/layouts.html#conversionWord) can be used (in addition to the standard static variable interpolations like `${HOSTNAME}`).
- `allowEmpty` (optional, default `false`): Normally, if the `value` results in a `null` or empty string, the field will not be sent. If `allowEmpty` is set to `true` then the field will be sent regardless
- `type` (optional, default `String`): type of the field on the resulting JSON message. Possible values are: `String`, `int`, `float` and `boolean`.
