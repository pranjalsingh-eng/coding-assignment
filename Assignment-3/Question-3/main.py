"""
main.py
-------
Entry point: detects the current OS, instantiates the correct host class,
fetches real-time hardware info, and displays it as JSON.

Usage:
    python main.py
"""

import platform
import sys

from host_info import LinuxHost, WindowsHost


def main() -> None:
    os_type = platform.system()  # 'Linux', 'Windows', 'Darwin', …

    if os_type == "Linux":
        host = LinuxHost()
    elif os_type == "Windows":
        host = WindowsHost()
    else:
        print(f"Unsupported OS: '{os_type}'. Only Linux and Windows are supported.")
        sys.exit(1)

    print(f"Detected OS : {os_type}")
    print(f"Host class  : {type(host).__name__}")
    print("-" * 60)

    host.get_hardware_info()
    host.display_hardware_info()


if __name__ == "__main__":
    main()