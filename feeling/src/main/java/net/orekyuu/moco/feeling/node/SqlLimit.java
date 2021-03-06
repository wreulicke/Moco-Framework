package net.orekyuu.moco.feeling.node;

import net.orekyuu.moco.feeling.SqlContext;
import net.orekyuu.moco.feeling.visitor.SqlVisitor;

public class SqlLimit extends SqlUnary {
    public SqlLimit(SqlNode node) {
        super(node);
    }

    @Override
    public void accept(SqlVisitor visitor, SqlContext context) {
        visitor.visit(this, context);
    }
}
