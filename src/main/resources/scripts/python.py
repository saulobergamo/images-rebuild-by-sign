def calculate_sum(a, b):
    return a + b

if __name__ == '__main__':
    num1 = float(input("Enter the first number: "))
    num2 = float(input("Enter the second number: "))
    result = calculate_sum(num1, num2)
    print("The sum is:", result)