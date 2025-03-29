package com.github.enesusta.logback.openobserve.encoder;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import com.github.enesusta.logback.openobserve.domain.Property;

public class AccessPropertyAndEncoder extends AbstractPropertyAndEncoder<IAccessEvent> {

  public AccessPropertyAndEncoder(Property property, Context context) {
    super(property, context);
  }

  @Override
  protected PatternLayoutBase<IAccessEvent> getLayout() {
    return new PatternLayout();
  }
}
