package com.github.enesusta.logback.openobserve.encoder;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import com.github.enesusta.logback.openobserve.domain.OpenObserveProperty;

public class ClassicPropertyAndEncoder extends AbstractPropertyAndEncoder<ILoggingEvent> {

  public ClassicPropertyAndEncoder(OpenObserveProperty property, Context context) {
    super(property, context);
  }

  @Override
  protected PatternLayoutBase<ILoggingEvent> getLayout() {
    return new PatternLayout();
  }
}
