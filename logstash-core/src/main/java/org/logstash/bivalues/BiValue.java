package org.logstash.bivalues;

import com.fasterxml.jackson.annotation.JsonValue;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import org.jruby.Ruby;
import org.jruby.runtime.builtin.IRubyObject;

public abstract class BiValue<R extends IRubyObject, J> implements Serializable {

    private static final long serialVersionUID = -8602478677605589528L;

    protected transient R rubyValue;
    protected J javaValue;

    public final R rubyValue(Ruby runtime) {
        if (hasRubyValue()) {
            return rubyValue;
        }
        addRuby(runtime);
        return rubyValue;
    }

    @JsonValue
    public J javaValue() {
        if (javaValue == null) {
            addJava();
        }
        return javaValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (hasJavaValue() && javaValue.getClass().isAssignableFrom(o.getClass())){
            return javaValue.equals(o);
        }

        if(!(o instanceof BiValue)) {
            return false;
        }

        BiValue<?, ?> other = (BiValue<?, ?>) o;

        return (other.hasJavaValue() && other.javaValue().equals(javaValue)) ||
                (other.hasRubyValue() && other.rubyValueUnconverted().equals(rubyValue));

    }

    @Override
    public final int hashCode() {
        if (hasRubyValue()) {
            return rubyValue.hashCode();
        }
        if (hasJavaValue()) {
            return javaValue.hashCode();
        }
        return 0;
    }

    public final R rubyValueUnconverted() {
        return rubyValue;
    }

    public boolean hasRubyValue() {
        return null != rubyValue;
    }

    public boolean hasJavaValue() {
        return null != javaValue;
    }

    protected abstract void addRuby(Ruby runtime);

    protected abstract void addJava();

    @Override
    public String toString() {
        if (hasRubyValue()) {
            javaValue();
        }
        if (javaValue == null) {
            return "";
        }
        return String.valueOf(javaValue);
    }

    protected static Object newProxy(BiValue instance) {
        return new SerializationProxy(instance);
    }

    private static final class SerializationProxy implements Serializable {
        private static final long serialVersionUID = -1749700725129586973L;

        private final Object javaValue;

        public SerializationProxy(BiValue o) {
            javaValue = o.javaValue(); // ensure the javaValue is converted from a ruby one if it exists
        }

        private Object readResolve() throws ObjectStreamException {
            return BiValues.newBiValue(javaValue);
        }
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }
}
