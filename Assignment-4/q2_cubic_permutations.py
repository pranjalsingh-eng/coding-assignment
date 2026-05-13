from collections import defaultdict


def solve():
    # key: sorted digits of cube, value: list of cubes with those digits
    cube_groups = defaultdict(list)

    n = 1
    while True:
        cube = n ** 3
        key = "".join(sorted(str(cube)))
        cube_groups[key].append(cube)

        if len(cube_groups[key]) == 5:
            print(f"Answer: {cube_groups[key][0]}")
            return

        n += 1


if __name__ == "__main__":
    solve()
