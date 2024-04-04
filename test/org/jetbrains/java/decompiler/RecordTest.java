package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jetbrains.java.decompiler.DecompilerTestFixture.assertFilesEqual;
import static org.junit.Assert.assertTrue;

public class RecordTest {

  private DecompilerTestFixture fixture;

  /*
   * Set individual test duration time limit to 60 seconds.
   * This will help us to test bugs hanging decompiler.
   */
  @Rule
  public Timeout globalTimeout = Timeout.seconds(60);

  @Before
  public void setUp() throws IOException {
    fixture = new DecompilerTestFixture();
    fixture.setUp(Map.of(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1",
      IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1",
      IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1",
      IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1",
      IFernflowerPreferences.CONVERT_PATTERN_SWITCH, "1",
      IFernflowerPreferences.CONVERT_RECORD_PATTERN, "1",
      IFernflowerPreferences.HIDE_RECORD_BODY, "1"
    ));
  }

  @After
  public void tearDown() throws IOException {
    fixture.tearDown();
    fixture = null;
  }

  @Test public void testRecordEmptyNoBody() { doTest("records/TestRecordEmptyNoBody"); }
  @Test public void testRecordSimpleNoBody() { doTest("records/TestRecordSimpleNoBody"); }
  @Test public void testRecordVarargNoBody() { doTest("records/TestRecordVarargNoBody"); }
  @Test public void testRecordGenericVarargNoBody() { doTest("records/TestRecordGenericVarargNoBody"); }
  @Test public void testRecordAnnoNoBody() { doTest("records/TestRecordAnnoNoBody"); }


  private void doTest(String testFile, String... companionFiles) {
    var decompiler = fixture.getDecompiler();

    var classFile = fixture.getTestDataDir().resolve("classes/" + testFile + ".class");
    assertThat(classFile).isRegularFile();
    for (var file : collectClasses(classFile)) {
      decompiler.addSource(file.toFile());
    }

    for (String companionFile : companionFiles) {
      var companionClassFile = fixture.getTestDataDir().resolve("classes/" + companionFile + ".class");
      assertThat(companionClassFile).isRegularFile();
      for (var file : collectClasses(companionClassFile)) {
        decompiler.addSource(file.toFile());
      }
    }

    decompiler.decompileContext();

    var decompiledFile = fixture.getTargetDir().resolve(classFile.getFileName().toString().replace(".class", ".java"));
    assertThat(decompiledFile).isRegularFile();
    assertTrue(Files.isRegularFile(decompiledFile));
    var referenceFile = fixture.getTestDataDir().resolve("results/" + classFile.getFileName().toString().replace(".class", ".dec"));
    assertThat(referenceFile).isRegularFile();
    assertFilesEqual(referenceFile, decompiledFile);
  }

  static List<Path> collectClasses(Path classFile) {
    var files = new ArrayList<Path>();
    files.add(classFile);

    var parent = classFile.getParent();
    if (parent != null) {
      var glob = classFile.getFileName().toString().replace(".class", "$*.class");
      try (DirectoryStream<Path> inner = Files.newDirectoryStream(parent, glob)) {
        inner.forEach(files::add);
      }
      catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }

    return files;
  }
}
