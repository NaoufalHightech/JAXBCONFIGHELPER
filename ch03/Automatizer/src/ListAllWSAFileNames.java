import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ListAllWSAFileNames {
    public static final String ROOT_WSA = "D:\\Projets\\frs\\frs-fsin\\frs-fsin-web\\src\\main\\java\\com\\groupama\\mrc\\frsm\\business\\service";

    public static void main(String[] args) throws IOException {

        Files.list(Path.of(ROOT_WSA)).filter(f -> f.toString().endsWith("WSAService.java") || f.toString().endsWith("WSService.java")).map(f -> f.getFileName().toFile().getName()).forEach(System.out::println);
    }
}
