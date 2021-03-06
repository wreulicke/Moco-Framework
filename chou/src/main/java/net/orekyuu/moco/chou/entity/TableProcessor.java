package net.orekyuu.moco.chou.entity;

import com.squareup.javapoet.JavaFile;
import net.orekyuu.moco.core.annotations.Table;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes("net.orekyuu.moco.core.annotations.Table")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TableProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment roundEnv) {
        if (typeElements.size() == 0) {
            return true;
        }

        for (TypeElement typeElement : typeElements) {
            for (Element element : roundEnv.getElementsAnnotatedWith(typeElement)) {
                Table table = element.getAnnotation(Table.class);
                if (table == null) {
                    continue;
                }
                Filer filer = super.processingEnv.getFiler();
                try {
                    Elements elementUtils = processingEnv.getElementUtils();
                    Types typeUtils = processingEnv.getTypeUtils();
                    EntityClassScanner entityClassScanner = new EntityClassScanner(table, elementUtils, typeUtils, processingEnv.getMessager());
                    entityClassScanner.scan(element);
                    List<JavaFile> javaFile = entityClassScanner.generatedFiles();
                    for (JavaFile file : javaFile) {
                        file.writeTo(filer);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return true;
    }
}
