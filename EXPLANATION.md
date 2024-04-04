## Setup
To achieve the test task, I changed FernFlower in such a way that it doesn't generate the redundant constructor
and accessors for `record` classes.

## Changes to the code
To achieve this, changes were made in the [ClassWriter](./src/org/jetbrains/java/decompiler/main/ClassWriter.java) class.
The changes are as follows:
1. (_line 868_) An extra check was added to the `methodToJava` method to check if the class is a record. 
The check consists of seeing if the class has record components and if the option to hide the record body is set.
If both conditions are met, then the method is hidden. This hides both the constructor and the accessors.
  ```java
    cl.getRecordComponents() != null && DecompilerContext.getOption(IFernflowerPreferences.HIDE_RECORD_BODY)
  ```
2. (_lines 270-272_) Because the method body was empty, the closing `}` was written on a newline.
To prevent this, and have the output in the form `public record Record(int x) {}` an additional statement was added 
that removes the newline character.
  ```java
  if(cl.getRecordComponents() != null && DecompilerContext.getOption(IFernflowerPreferences.HIDE_RECORD_BODY)) {
      buffer.setLength(buffer.length() - DecompilerContext.getNewLineSeparator().length());
    }
  ```
2. (_continued_) This is a purely cosmetic change and doesn't affect the functionality of the code, and that could have been left out.

## Option in the CLI
To enable this feature, a new option was added to the CLI. The option is `-hrb` and it is used to hide the record body.
By default, the record body is not hidden. To hide the record body, the option `-hrb=1` should be used.

This option was added to [IFernflowerPreferences](./src/org/jetbrains/java/decompiler/main/extern/IFernflowerPreferences.java), 
lines 39 and 88.

## Added tests
To ensure that the changes made to the code work as expected, tests were added to the
[RecordTest](./test/org/jetbrains/java/decompiler/RecordTest.java) class. All test originally found in `SimpleClassesTest`
that included a `record` were duplicated and adapted to match the new expected outcome. All test named `testRecord*` were 
adapted to match the new expected outcome. The new tests are named `testRecord*NoBody`. There are 4 other test that use 
files that contain `record`, but unfortunately, I was unable to compile into bytecode. This might be due to
the Java version I am using, or some other issues. I considered fixing this out of the scope of the task, but I would be
happy to investigate further if needed.

Without the -hrb option, all original test pass.

I additionally wanted to test a record declared inside a class, but this is only supported in Java 16 and I believe
FernFlower only supports up to Java 15. Even without my modification, the decompiled file is not correct 
(i.e. is missing the nested record altogether).