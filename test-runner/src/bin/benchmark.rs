use ansi_term::Colour::{Cyan, Green, Purple, Red, Yellow};
use std::collections::HashMap;
use std::env;
use std::fs;
use std::path::Path;
use std::process::Command;
use std::str::FromStr;

fn main() {
    let args: Vec<String> = env::args().collect();
    if args.len() < 2 {
        eprintln!(
            "{}",
            Red.paint("Usage: benchmark [interpreters...] [benchmark | --list]")
        );
        eprintln!(
            "  {}: 'clox' ({}), 'jlox' ({})",
            Purple.paint("Interpreters"),
            Yellow.paint("build/clox.exe"),
            Yellow.paint("java -cp build JLox.lox.Lox")
        );
        eprintln!("  {}:", Purple.paint("Examples"));
        eprintln!(
            "    benchmark clox jlox  # {}",
            Cyan.paint("Runs all benchmarks")
        );
        eprintln!(
            "    benchmark --list  # {}",
            Cyan.paint("Lists available benchmarks")
        );
        std::process::exit(1);
    }

    let mut interpreters = vec![];
    let mut benchmarks: Vec<String> = vec![];
    let mut list_only = false;

    // Parse args: interpreters first, then optional benchmark or --list
    let mut i = 1;
    while i < args.len() {
        if args[i] == "--list" {
            list_only = true;
            break;
        } else if Path::new(&format!("test/benchmark/{}.lox", args[i])).exists() {
            // Assume it's a benchmark name if it matches a file
            benchmarks.push(args[i].clone());
            break;
        } else {
            interpreters.push(args[i].clone());
            i += 1;
        }
    }

    if list_only {
        list_benchmarks();
        return;
    }

    if interpreters.is_empty() {
        eprintln!("{}", Red.paint("Error: No interpreter specified."));
        std::process::exit(1);
    }

    if benchmarks.is_empty() {
        // No specific benchmark: load all .lox files
        benchmarks = load_benchmarks();
        if benchmarks.is_empty() {
            eprintln!("{}", Red.paint("No .lox files found in test/benchmark/"));
            std::process::exit(1);
        }
    }

    // Sort benchmarks alphabetically for consistent order
    benchmarks.sort();

    if interpreters.len() > 1 {
        for benchmark in &benchmarks {
            println!("\n=== {}: {} ===", Purple.paint("Benchmark"), benchmark);
            run_comparison(&interpreters, benchmark);
        }
    } else {
        for benchmark in &benchmarks {
            println!("\n=== {}: {} ===", Purple.paint("Benchmark"), benchmark);
            run_benchmark(&interpreters[0], benchmark);
        }
    }
}

fn list_benchmarks() {
    let benchmarks = load_benchmarks();
    println!("{}:", Yellow.paint("Available benchmarks"));
    for b in &benchmarks {
        println!("  {}", Cyan.paint(b));
    }
}

fn load_benchmarks() -> Vec<String> {
    let benchmark_dir = Path::new("test/benchmark");
    if !benchmark_dir.exists() {
        return vec![];
    }
    let mut benchmarks = vec![];
    if let Ok(entries) = fs::read_dir(benchmark_dir) {
        for entry in entries.flatten() {
            let path = entry.path();
            if path.extension().and_then(|s| s.to_str()) == Some("lox") {
                if let Some(file_name) = path.file_stem().and_then(|s| s.to_str()) {
                    benchmarks.push(file_name.to_string());
                }
            }
        }
    }
    benchmarks
}

fn run_benchmark(interpreter: &str, benchmark: &str) {
    let mut trial = 1;
    let mut best = 9999.0;
    let mut no_improvement = 0;
    loop {
        let elapsed = run_trial(interpreter, benchmark);

        if elapsed < best {
            best = elapsed;
            no_improvement = 0;
        } else {
            no_improvement += 1;
        }

        let best_seconds = format!("{:.2}", best);
        println!(
            "{} #{} {} {} {}s",
            Yellow.paint("trial"),
            trial,
            interpreter,
            Purple.paint("best"),
            Green.paint(best_seconds)
        );
        trial += 1;

        if no_improvement >= 3 {
            break;
        }
    }
}

fn run_trial(interpreter: &str, benchmark: &str) -> f64 {
    let benchmark_path = format!("test/benchmark/{}.lox", benchmark);
    let output = match interpreter {
        "clox" => Command::new("build/clox.exe")
            .current_dir(env::current_dir().unwrap())
            .arg(&benchmark_path)
            .output()
            .expect("failed to execute clox"),
        "jlox" => Command::new("java")
            .current_dir(env::current_dir().unwrap())
            .args(["-cp", "build", "JLox.lox.Lox", &benchmark_path])
            .output()
            .expect("failed to execute jlox"),
        _ => panic!("Unsupported interpreter: {}", interpreter),
    };

    if !output.status.success() {
        panic!(
            "benchmark failed: {}",
            String::from_utf8_lossy(&output.stderr)
        );
    }

    let stdout = String::from_utf8_lossy(&output.stdout);
    let mut out_lines: Vec<&str> = stdout.lines().collect();
    if let Some(last) = out_lines.last() {
        if last.is_empty() {
            out_lines.pop();
        }
    }
    let elapsed_str = out_lines.last().expect("no output from benchmark");
    f64::from_str(elapsed_str).expect("invalid elapsed time")
}

fn run_comparison(interpreters: &[String], benchmark: &str) {
    let num_trials = 10;
    let mut best: HashMap<String, f64> = interpreters.iter().map(|i| (i.clone(), 9999.0)).collect();

    for trial in 1..=num_trials {
        for interpreter in interpreters {
            let elapsed = run_trial(interpreter, benchmark);
            if let Some(b) = best.get_mut(interpreter) {
                if elapsed < *b {
                    *b = elapsed;
                }
            }
        }

        println!("{}", Yellow.paint(format!("trial #{}", trial)));
        let mut best_time = 999.0;
        let mut worst_time = 0.0;
        let mut best_interpreter = String::new();
        for interpreter in interpreters {
            let b = best[interpreter];
            if b < best_time {
                best_time = b;
                best_interpreter = interpreter.clone();
            }
            if b > worst_time {
                worst_time = b;
            }
        }

        let worst_work = if worst_time > 0.0 {
            1.0 / worst_time
        } else {
            1.0
        };
        for interpreter in interpreters {
            let b = best[interpreter];
            #[allow(unused_assignments)]
            let mut suffix = String::new();
            let color = if interpreter == &best_interpreter {
                Green
            } else {
                Red
            };

            if interpreter == &best_interpreter {
                let best_work = if b > 0.0 { 1.0 / b } else { 0.0 };
                let work_ratio = best_work / worst_work;
                let faster = 100.0 * (work_ratio - 1.0);
                suffix = format!("{:.4}% faster", faster);
            } else {
                let ratio = if best_time > 0.0 { b / best_time } else { 0.0 };
                suffix = format!("{:.4}x time of best", ratio);
            }
            let best_string = format!("{:.4}", b);
            println!(
                " {:<30} {} {}s {}",
                interpreter,
                Purple.paint("best"),
                color.paint(best_string),
                color.paint(suffix)
            );
        }
    }
}
