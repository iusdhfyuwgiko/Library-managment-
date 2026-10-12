package com.library.controller;

import com.library.service.LibraryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
public class HomeController {
    private static final Map<String, String> SECTION_TARGETS = Map.ofEntries(
            Map.entry("dashboard", "dashboard"),
            Map.entry("books", "book-inventory"),
            Map.entry("members", "members-dashboard"),
            Map.entry("borrowing", "borrowing-panel"),
            Map.entry("returns", "returns-panel"),
            Map.entry("reservations", "reservations-panel"),
            Map.entry("categories", "category-insights"),
            Map.entry("authors", "authors"),
            Map.entry("fines", "returns-panel"),
            Map.entry("reports", "circulation-trend"),
            Map.entry("notifications", "recent-activity"),
            Map.entry("settings", "themeToggle")
    );

    private final LibraryService libraryService;

    public HomeController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/")
    public String index(@RequestParam(name = "section", defaultValue = "dashboard") String section, Model model) {
        if (!SECTION_TARGETS.containsKey(section)) {
            section = "dashboard";
        }
        return showSection(section, model);
    }

    @GetMapping("/sections/{section}")
    public String section(@PathVariable("section") String section) {
        if (!SECTION_TARGETS.containsKey(section)) {
            return "redirect:/";
        }
        return "redirect:/?section=" + section + "#" + SECTION_TARGETS.get(section);
    }

    private String showSection(String section, Model model) {
        model.addAttribute("books", libraryService.getAllBooks());
        model.addAttribute("activeSection", section);
        model.addAttribute("targetSection", SECTION_TARGETS.get(section));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }
}
