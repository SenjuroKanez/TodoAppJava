package com.todoapp.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * Represents a category for organizing Todo items.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category implements Serializable {
    private UUID id;
    private String name;
    private String color; // Hex color code
    private String userId;
    
    // Predefined category colors
    public static final String COLOR_RED = "#FF5D5D";
    public static final String COLOR_ORANGE = "#FFB067";
    public static final String COLOR_YELLOW = "#FFD166";
    public static final String COLOR_GREEN = "#06D6A0";
    public static final String COLOR_BLUE = "#118AB2";
    public static final String COLOR_PURPLE = "#9381FF";
    public static final String COLOR_PINK = "#EF476F";

    
}