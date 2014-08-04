AssertDeluxe
============

AssertDeluxe is a plugin for IntelliJ IDEA that simplifies creation of custom assertion class. Let's say you have a simple POJO
```java
public class Person {

    private String firstName;
    private String lastName;
    private int age;

    public Person(String firstName, String lastName, int age) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public int getAge() {
        return age;
    }
}
```java
With AssertDeluxe you can press ALT+Insert to generate Custom Assertion Class.
```
import org.assertj.core.api.Assertions;

public class PersonAssert {
    private final Person person;

    private PersonAssert(Person person) {
        this.person = person;
    }

    public static PersonAssert assertPerson(Person person) {
        return new PersonAssert(person);
    }

    public PersonAssert hasFirstName(String firstName) {
        Assertions.assertThat(person.getFirstName()).isEqualTo(firstName);
        return this;
    }

    public PersonAssert hasLastName(String lastName) {
        Assertions.assertThat(person.getLastName()).isEqualTo(lastName);
        return this;
    }

    public PersonAssert hasAge(int age) {
        Assertions.assertThat(person.getAge()).isEqualTo(age);
        return this;
    }
}
```
