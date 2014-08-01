package assertdeluxe;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiTypeElement;
import org.junit.Test;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

public class AssertClassCodeGeneratorTest {

    @Test
    public void codeForSourceClassField() throws Exception {
        // given
        AssertClassCodeGenerator generator = new AssertClassCodeGenerator(sourceClassWithName("SomeClass"));

        // when
        String codeForSourceClassField = generator.sourceClassField();

        // then
        assertThat(codeForSourceClassField).isEqualTo("private final SomeClass someClass;");
    }

    private PsiClass sourceClassWithName(String someClass) {
        PsiClass sourceClass = Mockito.mock(PsiClass.class);
        given(sourceClass.getName()).willReturn(someClass);
        return sourceClass;
    }

    @Test
    public void codeForConstructor() throws Exception {
        // given
        AssertClassCodeGenerator generator = new AssertClassCodeGenerator(sourceClassWithName("SomeClass"));

        // when
        String codeForConstructor = generator.constructor();

        // then
        assertThat(codeForConstructor).isEqualTo("private SomeClassAssert(SomeClass someClass) {this.someClass = someClass;}");
    }

    @Test
    public void codeForStaticAssertMethod() throws Exception {
        // given
        AssertClassCodeGenerator generator = new AssertClassCodeGenerator(sourceClassWithName("SomeClass"));

        // when
        String codeForStaticAssertMethod = generator.staticAssertMethod();

        // then
        assertThat(codeForStaticAssertMethod)
                .isEqualTo("public static SomeClassAssert assertSomeClass(SomeClass someClass) {return new SomeClassAssert(someClass);}");
    }

    @Test
    public void codeForFieldAssertMethod() throws Exception {
        // given
        AssertClassCodeGenerator generator = new AssertClassCodeGenerator(sourceClassWithName("SomeClass"));
        PsiField field = someField("String", "someField");

        // when
        String codeForFieldAssertMethod = generator.fieldAssertMethod(field);

        // then
        assertThat(codeForFieldAssertMethod).isEqualTo("public SomeClassAssert hasSomeField(String someField) " +
                "{org.assertj.core.api.Assertions.assertThat(someClass.getSomeField()).isEqualTo(someField);return this;}");
    }

    private PsiField someField(String type, String fieldName) {
        PsiField field = mock(PsiField.class);
        given(field.getName()).willReturn(fieldName);
        PsiTypeElement typeElement = mock(PsiTypeElement.class);
        given(typeElement.getText()).willReturn(type);
        given(field.getTypeElement()).willReturn(typeElement);
        return field;
    }
}
