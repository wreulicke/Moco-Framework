package net.orekyuu.moco.core.attribute;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Attribute<OWNER> {
    net.orekyuu.moco.feeling.attributes.Attribute attribute;
    private AttributeValueAccessor<OWNER> accessor;

    Attribute(net.orekyuu.moco.feeling.attributes.Attribute attribute, AttributeValueAccessor<OWNER> accessor) {
        this.attribute = attribute;
        this.accessor = accessor;
    }

    public abstract Class<?> bindType();

    public net.orekyuu.moco.feeling.attributes.Attribute ast() {
        return attribute;
    }

    public AttributeValueAccessor<OWNER> getAccessor() {
        return accessor;
    }

    public Predicate eq(Attribute value) {
        return new Predicate(attribute.eq(value.ast()));
    }

    public Predicate in(Attribute value) {
        return eq(value);
    }

    public Predicate in(Attribute ... value) {
        List<net.orekyuu.moco.feeling.attributes.Attribute> paramList = Stream.of(value)
                .distinct()
                .map(Attribute::ast)
                .collect(Collectors.toList());
        return new Predicate(attribute.in(paramList));
    }

    public Predicate not(Attribute value) {
        return new Predicate(attribute.noteq(value.ast()));
    }

    public Predicate gt(Attribute value) {
        return new Predicate(attribute.gt(value.ast()));
    }

    public Predicate gteq(Attribute value) {
        return new Predicate(attribute.gteq(value.ast()));
    }

    public Predicate lt(Attribute value) {
        return new Predicate(attribute.lt(value.ast()));
    }

    public Predicate lteq(Attribute value) {
        return new Predicate(attribute.lteq(value.ast()));
    }


}
