import yaml

def main():
    min_level = 1
    max_level = 50

    curve = []

    current_level = min_level
    while current_level <= max_level:
        curve.append(int(round(xp_to_next_level(current_level), -2)))
        current_level += 1

    titles = [1, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50]

    result = {
        'min-level': min_level,
        'max-level': max_level,
        'curve': curve,
        'titles': titles,
    }

    print(yaml.dump(result, allow_unicode=True))

def xp_to_next_level(current_level):
    return 100 * (current_level ** 1.6)

if __name__ == '__main__':
    main()
