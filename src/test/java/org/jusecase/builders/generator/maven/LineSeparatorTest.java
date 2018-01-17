package org.jusecase.builders.generator.maven;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class LineSeparatorTest {
    @Test
    public void system() {
        LineSeparator lineSeparator = LineSeparator.fromString("System");
        assertThat(lineSeparator).isEqualTo(LineSeparator.System);
        assertThat(lineSeparator.value).isEqualTo(System.lineSeparator());
    }

    @Test
    public void unix() {
        LineSeparator lineSeparator = LineSeparator.fromString("Unix");
        assertThat(lineSeparator).isEqualTo(LineSeparator.Unix);
        assertThat(lineSeparator.value).isEqualTo("\n");
    }

    @Test
    public void windows() {
        LineSeparator lineSeparator = LineSeparator.fromString("Windows");
        assertThat(lineSeparator).isEqualTo(LineSeparator.Windows);
        assertThat(lineSeparator.value).isEqualTo("\r\n");
    }

    @Test
    public void notSet() {
        assertThat(LineSeparator.fromString(null)).isEqualTo(LineSeparator.System);
    }

    @Test(expected = IllegalArgumentException.class)
    public void unkown() {
        LineSeparator.fromString("lsdfkdl");
    }
}