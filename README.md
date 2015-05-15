# ScalaTags 

ScalaTags is my attempt to simplify and extend the TaggedTypes introduced in Scalaz. 

It includes a simplistic annotation added to a class definition that locally adds in the defined Tag so we can easily force types in without adding runtime overhead from unwrapping

You start by importing the tags object from`import com.aire.Tags._`, then simplistically build up your tags. Inline tags via annotation take the type name of the declared variable, but in CamelCase.

For example:

```Scala
import com.aire.Tags._

case class Person(@tag firstName: String, @tag("LastName") last: String, age: Int)

object Instantiate {
  // Fails due to Jeff not being the right type and Blogs not being the right type
  val jeff = Person("Jeff", "Blogs") 
  
  // Fails due to Blogs not having the right type
  val jane = Person("Jane".as[Person#FirstName], "Blogs".as[Last])
  
  // Succeeds!
  val joe = Person("Joe".as[Person#FirstName], "Blogs".as[Person#LastName], 22)
}
```

We also have all of our standard tagging ability from Scalaz (Thanks Miles and Jason!)

```Scala
trait T
type TaggedInt = String @@ T
val tag: TaggedInt = "tag".as[T] 
```

We can also unwrap tagged types easily

```Scala
case class Person(@tag firstName: String, @tag("LastName") last: String, age: Int) {
  def fullName = s"$firstName $last"
}
```

Finally, we can reference the types declared in classes in the definition of other classes

```Scala
case class Person(@tag firstName: String, @tag("LastName") last: String, age: Int)
class PhoneNumber(@tag number: String, firstName: Person#FirstNameTag,
                       lastName: Person#LastNameTag)
```

Admittedly, this was mainly a learning experience for me using macro paradise, but hopefully it has some use!
