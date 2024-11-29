package com.github.enesusta.logback.openobserve.encoder;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import com.github.enesusta.logback.openobserve.domain.OpenObserveProperty;

public abstract class AbstractPropertyAndEncoder<T> {
  private OpenObserveProperty property;
  private PatternLayoutBase<T> layout;

  public AbstractPropertyAndEncoder(OpenObserveProperty property, Context context) {
    this.property = property;
    this.layout = getLayout();
    this.layout.setContext(context);
    this.layout.setPattern(property.getValue());
    this.layout.setPostCompileProcessor(null);
    this.layout.start();
  }

  protected abstract PatternLayoutBase<T> getLayout();

  public String encode(T event) {
    return layout.doLayout(event);
  }

  public String getName() {
    return property.getName();
  }

  public boolean allowEmpty() {
    return property.isAllowEmpty();
  }

  public OpenObserveProperty.Type getType() {
    return property.getType();
  }
}
