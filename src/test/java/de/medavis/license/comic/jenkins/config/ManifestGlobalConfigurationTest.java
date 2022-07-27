/*-
 * #%L
 * CoMiC - Component Manifest Creator
 * %%
 * Copyright (C) 2022 medavis GmbH
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package de.medavis.license.comic.jenkins.config;

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import java.net.URL;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.RestartableJenkinsRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

// TODO Add tests for error cases
public class ManifestGlobalConfigurationTest {

    private static final String COMPONENT_METADATA_URL = "https://component.metadata.url";
    private static final String LICENSES_URL = "https://licenses.url";
    private static final String LICENSES_MAPPING_URL = "https://licenses.mapping.url";

    @Rule
    public RestartableJenkinsRule reloadableJenkins = new RestartableJenkinsRule();

    @Test
    public void persistSettingDuringReload() {
        // TODO Check if we can prefix input field names to prevent collisions
        reloadableJenkins.then(jenkins -> {
            HtmlForm config = jenkins.createWebClient().goTo("configure").getFormByName("config");
            setInputValue(config, "_.componentMetadata", COMPONENT_METADATA_URL);
            setInputValue(config, "_.licenses", LICENSES_URL);
            setInputValue(config, "_.licenseMappings", LICENSES_MAPPING_URL);
            jenkins.submit(config);
            verifyStoredConfig("After submit");
        });
        reloadableJenkins.then(jenkins -> verifyStoredConfig("After restart"));
    }

    private void setInputValue(HtmlForm config, String name, String value) {
        HtmlTextInput textbox = config.getInputByName(name);
        textbox.setText(value);
    }

    private void verifyStoredConfig(String stage) {
        final ManifestGlobalConfiguration manifestGlobalConfiguration = ManifestGlobalConfiguration.getInstance();
        assertThat(manifestGlobalConfiguration).as(stage).satisfies(storedConfig -> {
            assertSoftly(softly -> {
                softly.assertThat(storedConfig.getComponentMetadata()).isEqualTo(COMPONENT_METADATA_URL);
                softly.assertThat(storedConfig.getComponentMetadataUrl()).map(URL::toString).hasValue(COMPONENT_METADATA_URL);
                softly.assertThat(storedConfig.getLicenses()).isEqualTo(LICENSES_URL);
                softly.assertThat(storedConfig.getLicensesUrl()).map(URL::toString).hasValue(LICENSES_URL);
                softly.assertThat(storedConfig.getLicenseMappings()).isEqualTo(LICENSES_MAPPING_URL);
                softly.assertThat(storedConfig.getLicenseMappingsUrl()).map(URL::toString).hasValue(LICENSES_MAPPING_URL);

            });
        });
    }
}
