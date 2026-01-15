use regex::Regex;
use std::collections::HashMap;
use std::collections::HashSet;
use std::env;
use std::fs;
use std::io::Write;
use std::path::Path;
use std::process::{Command, ExitCode};
use std::sync::OnceLock;

static EXPECTED_OUTPUT_PATTERN: &str = r"// expect: ?(.*)";
static EXPECTED_ERROR_PATTERN: &str = r"// (Error.*)";
static ERROR_LINE_PATTERN: &str = r"// \[((java|c) )?line (\d+)\] (Error.*)";
static EXPECTED_RUNTIME_ERROR_PATTERN: &str = r"// expect runtime error: (.+)";
static SYNTAX_ERROR_PATTERN: &str = r"\[.*line (\d+)\] (Error.+)";
static STACK_TRACE_PATTERN: &str = r"\[line (\d+)\]";
static NONTEST_PATTERN: &str = r"// nontest";

#[derive(Clone, Debug)]
struct Suite {
    #[allow(dead_code)]
    name: String,
    language: String,
    executable: String,
    args: Vec<String>,
    tests: HashMap<String, String>,
}

struct ExpectedOutput {
    line: usize,
    output: String,
}

struct Test {
    path: String,
    expected_output: Vec<ExpectedOutput>,
    expected_errors: HashSet<String>,
    expected_runtime_error: Option<String>,
    runtime_error_line: usize,
    expected_exit_code: i32,
    failures: Vec<String>,
}

#[derive(Debug)]
struct Config {
    filter_path: Option<String>,
    custom_interpreter: Option<String>,
    custom_arguments: Option<Vec<String>>,
}

// Global state using OnceLock for thread safety
static ALL_SUITES: OnceLock<HashMap<String, Suite>> = OnceLock::new();
static CONFIG: OnceLock<Config> = OnceLock::new();

struct RunnerState {
    passed: usize,
    failed: usize,
    skipped: usize,
    expectations: usize,
    suite: Option<Suite>,
}

impl RunnerState {
    fn new() -> Self {
        RunnerState {
            passed: 0,
            failed: 0,
            skipped: 0,
            expectations: 0,
            suite: None,
        }
    }
}

fn get_config() -> &'static Config {
    CONFIG.get().expect("Config not initialized")
}

fn main() -> ExitCode {
    let mut all_suites: HashMap<String, Suite> = HashMap::new();

    // Define jlox suite
    let jlox_tests = get_jlox_tests();
    all_suites.insert(
        "jlox".to_string(),
        Suite {
            name: "jlox".to_string(),
            language: "java".to_string(),
            executable: "java".to_string(),
            args: vec![
                "-cp".to_string(),
                "build".to_string(),
                "JLox.lox.Lox".to_string(),
            ],
            tests: jlox_tests,
        },
    );

    // Define clox suite
    let clox_tests = get_clox_tests();
    all_suites.insert(
        "clox".to_string(),
        Suite {
            name: "clox".to_string(),
            language: "c".to_string(),
            executable: "build/clox.exe".to_string(),
            args: vec![],
            tests: clox_tests,
        },
    );

    ALL_SUITES.set(all_suites).unwrap();

    let args: Vec<String> = env::args().collect();

    if args.len() < 2 {
        usage_error("Missing suite name.");
    }

    let suite_name = &args[1];
    let filter_path = if args.len() > 2 {
        Some(args[2].clone())
    } else {
        None
    };

    // Parse additional options
    let mut custom_interpreter = None;
    let mut custom_arguments = None;
    let mut i = 3;
    while i < args.len() {
        if args[i] == "--interpreter" || args[i] == "-i" {
            if i + 1 >= args.len() {
                usage_error("Missing value for --interpreter option.");
            }
            custom_interpreter = Some(args[i + 1].clone());
            i += 2;
        } else if args[i] == "--arguments" || args[i] == "-a" {
            if i + 1 >= args.len() {
                usage_error("Missing value for --arguments option.");
            }
            custom_arguments = Some(args[(i + 1)..].to_vec());
            i = args.len();
        } else {
            i += 1;
        }
    }

    let config = Config {
        filter_path,
        custom_interpreter,
        custom_arguments,
    };
    CONFIG.set(config).unwrap();

    if suite_name == "all" {
        let suites: Vec<String> = ALL_SUITES.get().unwrap().keys().cloned().collect();
        run_suites(&suites)
    } else if suite_name == "jlox" || suite_name == "clox" {
        let result = run_suite(suite_name);
        if result {
            ExitCode::SUCCESS
        } else {
            ExitCode::FAILURE
        }
    } else {
        eprintln!("Unknown interpreter '{}'", suite_name);
        ExitCode::FAILURE
    }
}

fn usage_error(message: &str) -> ! {
    eprintln!("{}", message);
    eprintln!("");
    eprintln!("Usage: main.rs <suite> [filter] [options]");
    eprintln!("");
    eprintln!("Options:");
    eprintln!("  -i, --interpreter <path>  Path to interpreter");
    eprintln!("  -a, --arguments <args>    Additional interpreter arguments");
    eprintln!("");
    eprintln!("Available suites: jlox, clox, all");
    std::process::exit(1);
}

fn get_jlox_tests() -> HashMap<String, String> {
    let mut tests = HashMap::new();

    // Early chapters - just scanning and expressions
    tests.insert("test/scanning".to_string(), "skip".to_string());
    tests.insert("test/expressions".to_string(), "skip".to_string());

    // JVM doesn't correctly implement IEEE equality on boxed doubles
    tests.insert(
        "test/number/nan_equality.lox".to_string(),
        "skip".to_string(),
    );

    // No hardcoded limits in jlox
    tests.insert(
        "test/limit/loop_too_large.lox".to_string(),
        "skip".to_string(),
    );
    tests.insert(
        "test/limit/no_reuse_constants.lox".to_string(),
        "skip".to_string(),
    );
    tests.insert(
        "test/limit/too_many_constants.lox".to_string(),
        "skip".to_string(),
    );
    tests.insert(
        "test/limit/too_many_locals.lox".to_string(),
        "skip".to_string(),
    );
    tests.insert(
        "test/limit/too_many_upvalues.lox".to_string(),
        "skip".to_string(),
    );
    tests.insert(
        "test/limit/stack_overflow.lox".to_string(),
        "skip".to_string(),
    );

    tests
}

fn get_clox_tests() -> HashMap<String, String> {
    let mut tests = HashMap::new();

    // Early chapters
    tests.insert("test/scanning".to_string(), "skip".to_string());
    tests.insert("test/expressions".to_string(), "skip".to_string());
    // No hard limit in CLOX (can have  2^24 constants)
    tests.insert(
        "test/limit/no_reuse_constants.lox".to_string(),
        "skip".to_string(),
    );
    tests.insert(
        "test/limit/too_many_constants.lox".to_string(),
        "skip".to_string(),
    );

    tests
}

fn run_suites(names: &[String]) -> ExitCode {
    let mut any_failed = false;

    for name in names {
        println!("=== {} ===", name);
        if !run_suite(name) {
            any_failed = true;
        }
    }

    if any_failed {
        ExitCode::FAILURE
    } else {
        ExitCode::SUCCESS
    }
}

/// Recursively collect all .lox files in a directory
fn collect_test_files(dir: &str) -> Vec<String> {
    let mut paths = Vec::new();
    let path = Path::new(dir);

    if path.is_dir() {
        if let Ok(entries) = fs::read_dir(path) {
            for entry in entries.flatten() {
                let entry_path = entry.path();
                if entry_path.is_dir() {
                    // Recurse into subdirectory
                    paths.extend(collect_test_files(entry_path.to_str().unwrap()));
                } else if entry_path.extension().and_then(|s| s.to_str()) == Some("lox") {
                    // Found a .lox file
                    if let Some(p) = entry_path.to_str() {
                        paths.push(p.to_string());
                    }
                }
            }
        }
    }
    paths
}

fn run_suite(name: &str) -> bool {
    let suites = ALL_SUITES.get().expect("Suites not initialized");
    let suite = match suites.get(name) {
        Some(s) => s.clone(),
        None => {
            eprintln!("Unknown suite: {}", name);
            return false;
        }
    };

    let mut state = RunnerState::new();
    state.suite = Some(suite.clone());

    // Find all .lox test files recursively
    let paths = collect_test_files("test");

    for path in paths {
        run_test(&path, &suite, &mut state);
    }

    // Print summary
    print!("\x1b[2K\r"); // Clear status line
    let _ = std::io::stdout().flush();

    if state.failed == 0 {
        println!(
            "All {} tests passed ({} expectations).",
            green(&state.passed.to_string()),
            state.expectations
        );
    } else {
        println!(
            "{} tests passed. {} tests failed.",
            green(&state.passed.to_string()),
            red(&state.failed.to_string())
        );
    }

    state.failed == 0
}

fn run_test(path: &str, suite: &Suite, state: &mut RunnerState) {
    if path.contains("benchmark") {
        return;
    }

    // Normalize path to use forward slashes
    let path_normalized = path.replace("\\", "/");

    let config = get_config();
    if let Some(filter) = &config.filter_path {
        if !path_normalized.contains(filter) {
            return;
        }
    }

    // Print status
    print!(
        "\x1b[2K\rPassed: {} Failed: {} Skipped: {} ({})",
        green(&state.passed.to_string()),
        red(&state.failed.to_string()),
        yellow(&state.skipped.to_string()),
        gray(&path_normalized)
    );
    let _ = std::io::stdout().flush();

    // Parse the test file
    let mut test = Test::new(&path_normalized, suite);

    if !test.parse(suite, state) {
        return;
    }

    let failures = test.run(suite);

    if failures.is_empty() {
        state.passed += 1;
    } else {
        state.failed += 1;
        println!("\n\x1b[2K\rFAIL {}", path);
        println!();
        for failure in &failures {
            println!("     {}", pink(failure));
        }
        println!();
    }
}

impl Test {
    fn new(path: &str, _suite: &Suite) -> Self {
        Test {
            path: path.to_string(),
            expected_output: Vec::new(),
            expected_errors: HashSet::new(),
            expected_runtime_error: None,
            runtime_error_line: 0,
            expected_exit_code: 0,
            failures: Vec::new(),
        }
    }

    fn parse(&mut self, suite: &Suite, state: &mut RunnerState) -> bool {
        // Get path components and find the state
        let parts: Vec<&str> = self.path.split('/').collect();
        let mut subpath = String::new();
        let mut state_str: Option<&str> = None;

        // Find the most specific matching state
        // We iterate through parts (e.g. "test", "assignment", "to_this.lox")
        // checking if "test", "test/assignment", or "test/assignment/to_this.lox"
        // has a defined state in suite.tests.
        for part in &parts {
            if !subpath.is_empty() {
                subpath.push('/');
            }
            subpath.push_str(part);

            if let Some(s) = suite.tests.get(&subpath) {
                state_str = Some(s.as_str());
            }
        }

        // FIX: Default to "run" if no state is explicitly defined.
        // The original Dart runner implicitly runs tests unless told to skip.
        let state_str = state_str.unwrap_or("run");

        if state_str == "skip" {
            state.skipped += 1;
            return false;
        }

        // Read and parse the test file
        let content = match fs::read_to_string(&self.path) {
            Ok(c) => c,
            Err(e) => {
                self.fail(&format!("Failed to read file: {}", e));
                return false;
            }
        };

        let lines: Vec<&str> = content.lines().collect();
        let re_expected_output = Regex::new(EXPECTED_OUTPUT_PATTERN).unwrap();
        let re_expected_error = Regex::new(EXPECTED_ERROR_PATTERN).unwrap();
        let re_error_line = Regex::new(ERROR_LINE_PATTERN).unwrap();
        let re_expected_runtime = Regex::new(EXPECTED_RUNTIME_ERROR_PATTERN).unwrap();
        let re_nontest = Regex::new(NONTEST_PATTERN).unwrap();

        for (line_num, &line) in lines.iter().enumerate() {
            let line_num = line_num + 1;

            // Check for nontest marker
            if re_nontest.is_match(line) {
                return false;
            }

            // Check for expected output
            if let Some(captures) = re_expected_output.captures(line) {
                if let Some(output) = captures.get(1) {
                    self.expected_output.push(ExpectedOutput {
                        line: line_num,
                        output: output.as_str().to_string(),
                    });
                    state.expectations += 1;
                }
                continue;
            }

            // Check for expected error
            if let Some(captures) = re_expected_error.captures(line) {
                if let Some(error) = captures.get(1) {
                    self.expected_errors
                        .insert(format!("[line {}] {}", line_num, error.as_str()));
                    self.expected_exit_code = 65; // EX_DATAERR
                    state.expectations += 1;
                }
                continue;
            }

            // Check for language-specific error line
            if let Some(captures) = re_error_line.captures(line) {
                // Capture group 2 is the language (optional)
                // If it exists and is not empty, check if it matches the suite language
                let language_match = captures
                    .get(2)
                    .map(|m| m.as_str())
                    .map(|lang| lang.is_empty() || lang == suite.language)
                    .unwrap_or(true); // If no language specified, it matches all suites

                if language_match {
                    if let Some(line_num_cap) = captures.get(3) {
                        if let Some(error) = captures.get(4) {
                            self.expected_errors.insert(format!(
                                "[line {}] {}",
                                line_num_cap.as_str(),
                                error.as_str()
                            ));
                            self.expected_exit_code = 65;
                            state.expectations += 1;
                        }
                    }
                }
                continue;
            }

            // Check for expected runtime error
            if let Some(captures) = re_expected_runtime.captures(line) {
                if let Some(error) = captures.get(1) {
                    self.runtime_error_line = line_num;
                    self.expected_runtime_error = Some(error.as_str().to_string());
                    self.expected_exit_code = 70; // EX_SOFTWARE
                    state.expectations += 1;
                }
            }
        }

        if !self.expected_errors.is_empty() && self.expected_runtime_error.is_some() {
            println!("TEST ERROR {}", self.path);
            println!("     Cannot expect both compile and runtime errors.");
            println!();
            return false;
        }

        true
    }

    fn run(&mut self, suite: &Suite) -> Vec<String> {
        let mut args: Vec<String> = Vec::new();

        let config = get_config();
        if let Some(ref custom_interpreter) = config.custom_interpreter {
            if let Some(ref custom_args) = config.custom_arguments {
                args.extend(custom_args.clone());
            }
            args.push(self.path.clone());

            let output = Command::new(custom_interpreter).args(&args).output();

            self.validate_output(output);
        } else {
            args.extend(suite.args.clone());
            args.push(self.path.clone());

            let output = Command::new(&suite.executable).args(&args).output();

            self.validate_output(output);
        }

        self.failures.clone()
    }

    fn validate_output(&mut self, output: Result<std::process::Output, std::io::Error>) {
        let output = match output {
            Ok(o) => o,
            Err(e) => {
                self.fail(&format!("Failed to run interpreter: {}", e));
                return;
            }
        };

        let stdout = String::from_utf8_lossy(&output.stdout);
        let stderr = String::from_utf8_lossy(&output.stderr);

        let stdout_lines: Vec<&str> = stdout.lines().collect();
        let stderr_lines: Vec<&str> = stderr.lines().collect();

        // Validate expected runtime error
        let expected_error_clone = self.expected_runtime_error.clone();
        if let Some(expected_error) = expected_error_clone {
            self.validate_runtime_error(&stderr_lines, &expected_error);
        } else {
            self.validate_compile_errors(&stderr_lines);
        }

        self.validate_exit_code(output.status.code(), &stderr_lines);
        self.validate_output_lines(&stdout_lines);
    }

    fn validate_runtime_error(&mut self, error_lines: &[&str], expected_error: &str) {
        if error_lines.len() < 2 {
            self.fail(&format!(
                "Expected runtime error '{}' and got none.",
                expected_error
            ));
            return;
        }

        if error_lines[0] != expected_error {
            self.fail(&format!(
                "Expected runtime error '{}' and got:",
                expected_error
            ));
            self.fail(error_lines[0]);
        }

        // Validate stack trace line
        let re_stack = Regex::new(STACK_TRACE_PATTERN).unwrap();
        let mut found_stack = false;
        let mut stack_line_num = 0;

        for line in &error_lines[1..] {
            if let Some(captures) = re_stack.captures(line) {
                if let Some(line_num) = captures.get(1) {
                    stack_line_num = line_num.as_str().parse().unwrap_or(0);
                    found_stack = true;
                    break;
                }
            }
        }

        if !found_stack {
            self.fail("Expected stack trace and got:");
            for line in &error_lines[1..] {
                self.fail(line);
            }
        } else if stack_line_num != self.runtime_error_line {
            self.fail(&format!(
                "Expected runtime error on line {} but was on line {}.",
                self.runtime_error_line, stack_line_num
            ));
        }
    }

    fn validate_compile_errors(&mut self, error_lines: &[&str]) {
        let re_syntax_error = Regex::new(SYNTAX_ERROR_PATTERN).unwrap();
        let mut found_errors = HashSet::new();
        let mut unexpected_count = 0;

        for line in error_lines {
            if let Some(captures) = re_syntax_error.captures(line) {
                if let Some(line_num) = captures.get(1) {
                    if let Some(error) = captures.get(2) {
                        // FIX: Use "line X" format to match expected errors
                        let error = format!("[line {}] {}", line_num.as_str(), error.as_str());
                        if self.expected_errors.contains(&error) {
                            found_errors.insert(error);
                        } else {
                            if unexpected_count < 10 {
                                self.fail("Unexpected error:");
                                self.fail(line);
                            }
                            unexpected_count += 1;
                        }
                    }
                }
            } else if !line.is_empty() {
                if unexpected_count < 10 {
                    self.fail("Unexpected output on stderr:");
                    self.fail(line);
                }
                unexpected_count += 1;
            }
        }

        if unexpected_count > 10 {
            self.fail(&format!("(truncated {} more...)", unexpected_count - 10));
        }

        // Check for missing expected errors
        for error in self.expected_errors.clone() {
            if !found_errors.contains(&error) {
                self.fail(&format!("Missing expected error: {}", error));
            }
        }
    }

    fn validate_exit_code(&mut self, exit_code: Option<i32>, error_lines: &[&str]) {
        let exit_code = exit_code.unwrap_or(-1);

        if exit_code == self.expected_exit_code {
            return;
        }

        let display_lines: Vec<&str> = if error_lines.len() > 10 {
            let mut lines = error_lines[..10].to_vec();
            lines.push("(truncated...)");
            lines
        } else {
            error_lines.to_vec()
        };

        self.fail(&format!(
            "Expected return code {} and got {}. Stderr:",
            self.expected_exit_code, exit_code
        ));
        self.fail_list(display_lines);
    }

    fn validate_output_lines(&mut self, output_lines: &[&str]) {
        let mut lines: Vec<&str> = output_lines.to_vec();

        // Remove trailing empty line
        if let Some(last) = lines.last() {
            if last.is_empty() {
                lines.pop();
            }
        }

        let mut index = 0;
        let expected_len = self.expected_output.len();

        while index < lines.len() && index < expected_len {
            let line = lines[index];
            let expected = &self.expected_output[index];

            if expected.output != line {
                self.fail(&format!(
                    "Expected output '{}' on line {} and got '{}'.",
                    expected.output, expected.line, line
                ));
            }
            index += 1;
        }

        while index < expected_len {
            let expected = &self.expected_output[index];
            self.fail(&format!(
                "Missing expected output '{}' on line {}.",
                expected.output, expected.line
            ));
            index += 1;
        }

        if index < lines.len() {
            self.fail(&format!(
                "Got output '{}' when none was expected.",
                lines[index]
            ));
        }
    }

    fn fail(&mut self, message: &str) {
        self.failures.push(message.to_string());
    }

    fn fail_list(&mut self, lines: Vec<&str>) {
        for line in lines {
            self.fail(line);
        }
    }
}

// ANSI color functions
fn green(s: &str) -> String {
    format!("\x1b[32m{}\x1b[0m", s)
}

fn red(s: &str) -> String {
    format!("\x1b[31m{}\x1b[0m", s)
}

fn yellow(s: &str) -> String {
    format!("\x1b[33m{}\x1b[0m", s)
}

fn gray(s: &str) -> String {
    format!("\x1b[90m{}\x1b[0m", s)
}

fn pink(s: &str) -> String {
    format!("\x1b[95m{}\x1b[0m", s)
}
