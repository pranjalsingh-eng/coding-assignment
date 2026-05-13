import subprocess
import time


def execute_commands(commands: list) -> list:
    """
    Executes a list of OS commands and returns results as a list of dictionaries.

    Args:
        commands: List of OS command strings to execute.

    Returns:
        A list containing one dictionary per unique command with keys:
        command, output, error, status, and execution_time_sec.
    """
    results = []
    seen = set()

    for command in commands:
        # Skip duplicates
        if command in seen:
            continue
        seen.add(command)

        try:
            start = time.time()
            process = subprocess.run(
                command,
                shell=True,
                capture_output=True,
                text=True
            )
            elapsed = round(time.time() - start, 4)

            if process.returncode == 0:
                results.append({
                    command: {
                        "output": process.stdout.strip(),
                        "error": "",
                        "status": "success",
                        "execution_time_sec": elapsed
                    }
                })
            else:
                results.append({
                    command: {
                        "output": process.stdout.strip(),
                        "error": process.stderr.strip(),
                        "status": "Failed",
                        "execution_time_sec": elapsed
                    }
                })

        except Exception as e:
            results.append({
                command: {
                    "output": "",
                    "error": str(e),
                    "status": "Failed",
                    "execution_time_sec": 0
                }
            })

    return results


if __name__ == "__main__":
    import json

    # Sample command list (includes duplicates and an invalid command)
    commands = ["dir", "cd", "hostname", "dir", "invalid_command", "whoami"]

    output = execute_commands(commands)

    print(json.dumps(output, indent=4))

    with open("output.json", "w") as f:
        json.dump(output, f, indent=4)

    print("\nResults saved to output.json")
