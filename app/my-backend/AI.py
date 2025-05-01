import math


def main():
    x, y = map(int, input().split())
    attacks = math.ceil(x / y)

    print(attacks)


if __name__ == '__main__':
    main()
