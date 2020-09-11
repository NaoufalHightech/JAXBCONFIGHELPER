import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

public class Automatize {
    public static final String ROOT_WSDL = "D:\\Projets\\frs\\frs-wss-proxy\\src\\main\\resources\\META-INF\\wsdl\\cics";
    public static final String TEMPLATE_PARAM = "__WSDL__";
    public static final String TEMPLATE_TARGET_NS_PARAM = "__WSDL_NS__";
    public static final String PROPERTIES_FILE = "D:\\Projets\\frs\\frs-wss-proxy\\src\\main\\scripts\\build.properties";
    public static final String ROOT_BINDING = "D:\\Projets\\frs\\frs-wss-proxy\\src\\main\\resources\\META-INF\\bindings\\cics";
    public static final String TEMPLATE_BUSINESS_NAME = "__ID__";
    public static final String PACKAGE_NAME = "__PACKAGE__";

    /*
    # Services web : ESBM00 - Lire Sous Parcelle
ws.name.service.esbm00=LireSousParcelle
ws.namespace.service.esbm00=LireSousParcelle_V450_20200107_161643
ws.namespace2.service.esbm00=LireSousParcelle_Entree
ws.namespace3.service.esbm00=LireSousParcelle_Sortie
ws.package.service.esbm00=com.groupama.service.cics.client.esbm00

<definitions targetNamespace="AnnulerSupprimerReglements_V451_20200609_160953" xmlns="http://schemas.xmlsoap.org/wsdl/"

*/
    public static final String TEMAPLTE_XML_BINDING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
            "<jaxws:bindings version=\"2.0\"\n" +
            "\t\twsdlLocation=\"../../wsdl/cics/__WSDL__.wsdl\"\n" +
            "\t\txmlns:xs=\"http://www.w3.org/2001/XMLSchema\"\n" +
            "\t\txmlns:wsdl=\"http://schemas.xmlsoap.org/wsdl/\"\n" +
            "\t\txmlns:jaxws=\"http://java.sun.com/xml/ns/jaxws\"\n" +
            "\t\txmlns:jaxb=\"http://java.sun.com/xml/ns/jaxb\">\n" +
            "    <jaxws:bindings node=\"wsdl:definitions[@targetNamespace='__WSDL_NS__']\">\n" +
            "   \t\t<jaxws:package name=\"__PACKAGE__\" />\n" +
            "    </jaxws:bindings>\n" +
            "\t<jaxb:bindings node=\"wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='__WSDL___Entree']\">\n" +
            "\t\t<jaxb:schemaBindings>\n" +
            "\t\t</jaxb:schemaBindings>\n" +
            "\t\t\t<jaxb:package name=\"__PACKAGE__.in\" />\n" +
            "\t</jaxb:bindings>\n" +
            "\t<jaxb:bindings node=\"wsdl:definitions/wsdl:types/xs:schema[@targetNamespace='__WSDL___Sortie']\">\n" +
            "\t\t<jaxb:schemaBindings>\n" +
            "\t\t\t<jaxb:package name=\"__PACKAGE__.out\" />\n" +
            "\t\t</jaxb:schemaBindings>\n" +
            "\t</jaxb:bindings>\n" +
            "</jaxws:bindings>";

    public static final String TEMPLATE_POM = "<execution>\n" +
            "                        <id>__ID__ __WSDL__</id>\n" +
            "                        <phase>generate-sources</phase>\n" +
            "                        <goals>\n" +
            "                            <goal>wsimport</goal>\n" +
            "                        </goals>\n" +
            "                        <configuration>\n" +
            "                            <wsdlFiles>\n" +
            "                                <wsdlFile>cics/__WSDL__.wsdl</wsdlFile>\n" +
            "                            </wsdlFiles>\n" +
            "                            <wsdlLocation>/META-INF/wsdl/cics/__WSDL__.wsdl</wsdlLocation>\n" +
            "                            <staleFile>${basedir}/target/stale/wsdl.__WSDL__.done</staleFile>\n" +
            "                            <bindingFiles>\n" +
            "                                <bindingFile>global-jaxb-bindings.xml</bindingFile>\n" +
            "                                <bindingFile>cics/__WSDL__-jaxws-bindings.xml</bindingFile>\n" +
            "                            </bindingFiles>\n" +
            "                        </configuration>\n" +
            "                    </execution>";

    public static void main(String[] args) throws IOException {
        // List lines of properties
        var nameToCode = Files.lines(Path.of(PROPERTIES_FILE), StandardCharsets.ISO_8859_1)
                .map(l -> l.split("="))
                .filter(parts -> parts.length == 2 && parts[0].startsWith("ws.name.service.") && !parts[1].contains("."))
                .collect(toMap(parts -> parts[1].trim(), parts -> {
                    var trimSubs = parts[0].trim().split("\\.");
                    return trimSubs[trimSubs.length - 1];
                }));

        // ws.package.service.esbm00=com.groupama.service.cics.client.esbm00
        var nameToPackage = Files.lines(Path.of(PROPERTIES_FILE), StandardCharsets.ISO_8859_1)
                .map(l -> l.split("="))
                .filter(parts -> parts.length == 2 && parts[0].startsWith("ws.package.service."))
                .collect(toMap(parts -> {
                    var trimSubs = parts[1].trim().split("\\.");
                    return trimSubs[trimSubs.length - 1];
                }, parts -> parts[1].trim()));

        // <definitions targetNamespace="AnnulerSupprimerReglements_V451_20200609_160953" xmlns="http://schemas.xmlsoap.org/wsdl/"
        System.out.println(nameToCode);
        System.out.println(nameToPackage);

        nameToCode.forEach((k, v) -> System.out.println("package name : " +  k +" : "+ v));
        nameToPackage.forEach((k, v) -> System.out.println("package name : " + k +" : "+v));
        // List WSDL files

        Files.list(Path.of(ROOT_WSDL)).filter(f -> f.toString().endsWith(".wsdl")).forEach(
                f -> {
                    var fileName = f.getFileName().toFile().getName();
                    var simpleName = fileName.substring(0, fileName.length() - 5);
                    var businessName = nameToCode.get(simpleName);
                    var packageName = nameToPackage.get(businessName);

                    if(businessName == null) {
                        System.err.println("Simple filename no match to business name " + simpleName);
                        return;
                    }
                    // Open fileName to extract TragteNamespace
                    Optional<String> targetNamespaceLine = null;
                    Pattern TARGET_NAMESPACE_PATTERN = Pattern.compile("\"(?<=targetNamespace=\\\")(\\w*)(?=\\\" )\"");
                    var targetNamespace = "";
                    try {
                        //todo check other cases where the regex dont match
                        // <definitions targetNamespace="AnnulerSupprimerReglements_V451_20200609_160953" xmlns="http://schemas.xmlsoap.org/wsdl/"
                        targetNamespaceLine = Files.lines(f).filter(l -> l.contains("targetNamespace")).findFirst();
                        if( targetNamespaceLine.isPresent()){
                            Matcher matcher = TARGET_NAMESPACE_PATTERN.matcher(targetNamespaceLine.get());
                            if (matcher.find()) {
                                targetNamespace = matcher.group(1);
                                System.out.println(targetNamespace);
                            }
                        }

                    } catch(IOException e) {
                        e.printStackTrace();
                    }

                    if(packageName == null) {
                        System.err.println("Package Name no match to business name " + businessName);
                        return;
                    }
                    var result = TEMPLATE_POM.replaceAll(TEMPLATE_PARAM, simpleName).replaceAll(TEMPLATE_BUSINESS_NAME, businessName);
                    // Replace Package
                    if(targetNamespace == null) {
                        System.err.println("Package Name no match to business name " + businessName);
                        return;
                    }
                    var resultXmlFile = TEMAPLTE_XML_BINDING.replaceAll(TEMPLATE_TARGET_NS_PARAM, targetNamespace).replaceAll(TEMPLATE_PARAM, simpleName).replaceAll(PACKAGE_NAME, packageName);

                    var targetBindBing = new File(ROOT_BINDING, simpleName + "-jaxws-bindings.xml");
                    try(var writer = Files.newBufferedWriter(targetBindBing.toPath());) {

                        // Print it
                        writer.write(resultXmlFile);

                    } catch(FileNotFoundException e) {
                        e.printStackTrace();
                    } catch(IOException e) {
                        e.printStackTrace();
                    }

                });
    }
}
