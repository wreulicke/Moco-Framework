package net.orekyuu.moco.chou;

import com.squareup.javapoet.*;
import net.orekyuu.moco.core.annotations.Column;
import net.orekyuu.moco.feeling.Select;
import net.orekyuu.moco.feeling.Table;
import net.orekyuu.moco.feeling.TableBuilder;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class TableClassFields {
    private TableClassFields() {
        throw new UnsupportedOperationException();
    }

    public static FieldSpec mapper(OriginalEntity originalEntity) {
        ParameterizedTypeName mapperType = ParameterizedTypeName.get(ClassName.get(Select.QueryResultMapper.class), originalEntity.originalClassName());


        TypeSpec.Builder mapperClass = TypeSpec.anonymousClassBuilder("")
                .addSuperinterface(mapperType);

        MethodSpec.Builder mappingMethod = MethodSpec.methodBuilder("mapping")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .returns(originalEntity.originalClassName())
                .addParameter(ParameterSpec.builder(ClassName.get(ResultSet.class), "resultSet").build())
                .addException(ClassName.get(SQLException.class))
                .addException(ClassName.get(ReflectiveOperationException.class));

        mappingMethod.addStatement("$T record = new $T()", originalEntity.getOriginalType(), originalEntity.getOriginalType());
        CodeBlock.Builder initializer = CodeBlock.builder();
        initializer.beginControlFlow("try");
        for (ColumnField columnField : originalEntity.getColumnFields()) {
            Column column = columnField.getColumn();
            String fieldName = columnField.getVariableElement().getSimpleName().toString();
            String columnName = column.name();

            // add field
            mapperClass.addField(FieldSpec.builder(ClassName.get(Field.class), fieldName).addModifiers(Modifier.PRIVATE).build());
            // initialize field
            initializer.addStatement("$L = $T.class.getDeclaredField($S)", fieldName, originalEntity.originalClassName(), fieldName);
            initializer.addStatement("$L.setAccessible(true)", fieldName);
            // mapper
            mappingMethod.addStatement("$L.set(record, resultSet.getObject($S));", fieldName, columnName);
        }
        mappingMethod.addStatement("return record");
        // end constructor
        initializer.nextControlFlow("catch ($T e)", ReflectiveOperationException.class)
                .addStatement("throw new $T(e)", RuntimeException.class)
                .endControlFlow();

        mapperClass.addInitializerBlock(initializer.build());
        mapperClass.addMethod(mappingMethod.build());

        TypeSpec anonymous = mapperClass.build();

        return FieldSpec.builder(mapperType, "MAPPER", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", anonymous)
                .build();
    }

    public static FieldSpec tableField(OriginalEntity originalEntity) {
        CodeBlock.Builder block = CodeBlock.builder().add("new $T($S, MAPPER)", TableBuilder.class, originalEntity.getTable().name());
        for (ColumnField field : originalEntity.getColumnFields()) {
            VariableElement element = field.getVariableElement();
            Optional<DatabaseColumnType> type = DatabaseColumnType.findSupportedType(element);
            DatabaseColumnType databaseColumnType = type.orElseThrow(RuntimeException::new);
            databaseColumnType.addColumnMethod(block, field.getColumn().name());
        }
        block.add(".build()");
        return FieldSpec.builder(Table.class, "TABLE", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(block.build())
                .build();
    }

    public static FieldSpec columnField(OriginalEntity originalEntity, ColumnField field) {
        String fieldName = field.tableClassColumnName();

        ClassName attributeClass = field.getAttributeClass();
        CodeBlock.Builder builder = CodeBlock.builder().add("new $T<>(TABLE.$L($S), $T::$L)",
                attributeClass, field.getFeelingTableMethod(), field.getColumn().name(), originalEntity.originalClassName(), field.entityGetterMethod());

        return FieldSpec.builder(
                ParameterizedTypeName.get(attributeClass, originalEntity.originalClassName()),
                fieldName,
                Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                .initializer(builder.build())
                .build();
    }
}