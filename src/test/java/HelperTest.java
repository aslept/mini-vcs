import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;

public class HelperTest {
    public static class Dude implements Serializable {
        String name;
        public Dude(String n) {
            name = n;
        }
    }

    String testPath = "src/test/java/testfiles/aaa.txt";

    @Test
    public void sameClass() {
        Dude d = new Dude("Takumi");

        Helper.objectWrite(d, testPath);
        Dude newDude = (Dude) Helper.objectRead(testPath);

        System.out.println("Same name? " + d.name.equals(newDude.name));
        System.out.println("Name: " + d.name + " " + newDude.name);

        try {
            Files.delete(Path.of(testPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assertTrue(d.name.equals(newDude.name));
    }

    @Test
    public void fileHashTest() {
        Path path = Path.of("src/test/java/testfiles/file.txt");
        String expectedHash = "bcc5579ba0c69487ea8b5be888cf0925e18f8d01";

        String actualHash = Helper.sha1(path);

        System.out.println("Expected: " + expectedHash + "\nActual: " + actualHash);

        assertEquals(expectedHash, actualHash);
    }

    @Test
    public void sha1CheckTest() {
        Path path = Path.of("src/test/java/testfiles/file.txt");
        String expectedHash = "bcc5579ba0c69487ea8b5be888cf0925e18f8d01";

        boolean bool = Helper.sha1Checker(path, expectedHash);

        System.out.println("Same? " + bool);

        assertTrue(bool);
    }
}
