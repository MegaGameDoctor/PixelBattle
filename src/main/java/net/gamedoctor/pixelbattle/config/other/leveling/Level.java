package net.gamedoctor.pixelbattle.config.other.leveling;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Level {
    private final int needExp;
    private final int paintCooldown;
}