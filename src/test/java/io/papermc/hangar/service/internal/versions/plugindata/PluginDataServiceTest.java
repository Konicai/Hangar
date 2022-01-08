package io.papermc.hangar.service.internal.versions.plugindata;

import io.papermc.hangar.exceptions.HangarApiException;
import io.papermc.hangar.model.api.project.version.PluginDependency;
import io.papermc.hangar.model.common.Platform;
import io.papermc.hangar.service.internal.versions.plugindata.handler.PaperFileTypeHandler;
import io.papermc.hangar.service.internal.versions.plugindata.handler.VelocityFileTypeHandler;
import io.papermc.hangar.service.internal.versions.plugindata.handler.WaterfallFileTypeHandler;
import io.papermc.hangar.service.internal.versions.plugindata.scanner.JarScanner;
import io.papermc.hangar.service.internal.versions.plugindata.scanner.checks.OpenConnectionMethodInsnCheck;
import io.papermc.hangar.service.internal.versions.plugindata.scanner.checks.SetOpMethodInsnCheck;
import io.papermc.hangar.service.internal.versions.plugindata.scanner.checks.ThreadSleepMethodInsnCheck;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {PluginDataService.class, PaperFileTypeHandler.class, WaterfallFileTypeHandler.class, VelocityFileTypeHandler.class, JarScanner.class,
    SetOpMethodInsnCheck.class, ThreadSleepMethodInsnCheck.class, OpenConnectionMethodInsnCheck.class})
class PluginDataServiceTest {

    private static final Path path = Path.of("src/test/resources/io/papermc/hangar/service/internal/versions/plugindata");

    @Autowired
    private PluginDataService classUnderTest;

    @Test
    void testLoadPaperPluginMetadata() throws Exception {
        PluginFileData data = classUnderTest.loadMeta(path.resolve("Paper.jar"), -1).getData();
        data.validate();
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertIterableEquals(List.of("KennyTV"), data.getAuthors().get(Platform.PAPER));

        Set<PluginDependency> deps = data.getDependencies().get(Platform.PAPER);
        assertThat(deps).hasSize(3);
        assertThat(deps).extracting(PluginDependency::getName).containsExactlyInAnyOrder("ProtocolLib", "ServerListPlus", "ProtocolSupport");
        assertThat(deps).extracting(PluginDependency::isRequired).containsOnly(false);

        assertIterableEquals(Set.of("1.13"), data.getPlatformDependencies().get(Platform.PAPER));
    }

    @Test
    void testLoadWaterfallPluginMetadata() throws Exception {
        PluginFileData data = classUnderTest.loadMeta(path.resolve("Waterfall.jar"), -1).getData();

        data.validate();
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertIterableEquals(List.of("KennyTV"), data.getAuthors().get(Platform.WATERFALL));

        Set<PluginDependency> deps = data.getDependencies().get(Platform.WATERFALL);
        assertThat(deps)
                .hasSize(2)
                .anyMatch(pd -> pd.getName().equals("ServerListPlus") && !pd.isRequired())
                .anyMatch(pd -> pd.getName().equals("SomePlugin") && pd.isRequired());
    }

    @Test
    void testLoadVelocityPluginMetadata() throws Exception {
        PluginFileData data = classUnderTest.loadMeta(path.resolve("Velocity.jar"), -1).getData();

        data.validate();
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertIterableEquals(List.of("KennyTV"), data.getAuthors().get(Platform.VELOCITY));

        Set<PluginDependency> deps = data.getDependencies().get(Platform.VELOCITY);
        assertThat(deps)
                .hasSize(1)
                .anyMatch(pd -> pd.getName().equals("serverlistplus") && !pd.isRequired());
    }

    @Test
    void testLoadPaperPluginZipMetadata() throws Exception {
        PluginFileData data = classUnderTest.loadMeta(path.resolve("TestZip.zip"), -1).getData();

        data.validate();
        assertEquals("Maintenance", data.getName());
        assertEquals("Enable maintenance mode with a custom maintenance motd and icon.", data.getDescription());
        assertEquals("3.0.5", data.getVersion());
        assertIterableEquals(List.of("KennyTV"), data.getAuthors().get(Platform.PAPER));

        Set<PluginDependency> deps = data.getDependencies().get(Platform.PAPER);
        assertThat(deps).hasSize(3);
        assertThat(deps).extracting(PluginDependency::getName).containsExactlyInAnyOrder("ProtocolLib", "ServerListPlus", "ProtocolSupport");
        assertThat(deps).extracting(PluginDependency::isRequired).containsOnly(false);

        assertIterableEquals(Set.of("1.13"), data.getPlatformDependencies().get(Platform.PAPER));
    }

    @ParameterizedTest
    @CsvSource({
            "EmptyMeta.jar,version.new.error.metaNotFound",
            "Empty.jar,version.new.error.metaNotFound",
            "IncompleteMeta.jar,version.new.error.incomplete",
            "Empty.zip,version.new.error.jarNotFound"
    })
    void testLoadMetaShouldFail(String jarName, String expectedMsg) {
        Path jarPath = path.resolve(jarName);
        HangarApiException exception = assertThrows(HangarApiException.class, () -> {
            classUnderTest.loadMeta(jarPath, -1);
        });
        assertEquals("400 BAD_REQUEST \"" + expectedMsg + "\"", exception.getMessage());
    }

    //@Test
    void dum() throws IOException {
        System.out.println("scan paper.jar");
        PluginFileData data = classUnderTest.loadMeta(path.resolve("testplugins-paper-1.0.0-SNAPSHOT.jar"), -1).getData();
        data.validate();
/*         System.out.println("scan velocity.jar");
        data = classUnderTest.loadMeta(path.resolve("testplugins-velocity-1.0.0-SNAPSHOT.jar"), -1).getData();
        data.validate();
        System.out.println("scan waterfall.jar");
        data = classUnderTest.loadMeta(path.resolve("testplugins-waterfall-1.0.0-SNAPSHOT.jar"), -1).getData();
        data.validate(); */
    }

    //@Test
    void dummer() throws IOException {
        Path path = Path.of("C:\\Users\\Martin\\Downloads\\malware\\plugins");
        Files.list(path).forEach(f -> {
            System.out.println("scan " + f.getFileName());
            try {
                PluginFileData data = classUnderTest.loadMeta(f, -1).getData();
                data.validate();
            } catch (HangarApiException e) {
                System.out.println(e.getMessage());
            } catch (Exception e) {
                System.out.println(e.getClass().getName() + " " + e.getMessage());
            }
        });
    }
}
