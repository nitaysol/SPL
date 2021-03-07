import sqlite3
import os.path
import sys
CONST_DB_NAME = "schedule.db"
CONST_BAD_CHARS = '\n\t\r '


def print_tuple_list(list_of_tuples):
    for item in list_of_tuples:
        print(item)


def print_tables(cursor_object):
    select_all = """SELECT * FROM {}"""
    print("courses")
    print_tuple_list((cursor_object.execute(select_all.format("courses"))).fetchall())

    print("classrooms")
    print_tuple_list((cursor_object.execute(select_all.format("classrooms"))).fetchall())

    print("students")
    print_tuple_list((cursor_object.execute(select_all.format("students", ))).fetchall())


def add_to_tables(cursor_object):
    configuration_file = sys.argv[1] + ".txt"
    insert_into_courses = """INSERT INTO courses VALUES (?, ?, ?, ?, ?, ?)"""
    insert_into_students = """INSERT INTO students VALUES (?, ?)"""
    insert_into_classrooms = """INSERT INTO classrooms VALUES (?, ?, ?, ?)"""
    with open(configuration_file, "r") as file_stream:
        for line in file_stream:
            current_line = line.split(",")
            current_line = [i.strip(CONST_BAD_CHARS) for i in current_line]
            if current_line[0] == "C":
                cursor_object.execute(insert_into_courses, (current_line[1], current_line[2], current_line[3],
                                      current_line[4], current_line[5], current_line[6]))
            elif current_line[0] == "S":
                cursor_object.execute(insert_into_students, (current_line[1], current_line[2]))
            else:
                cursor_object.execute(insert_into_classrooms, (current_line[1], current_line[2].strip('\n'), "0", "0"))


def create_db():
    sql_create_courses = """CREATE TABLE courses (
                                            id INTEGER PRIMARY KEY,
                                            course_name TEXT NOT NULL,
                                            student TEXT NOT NULL,
                                            number_of_students INTEGER NOT NULL,
                                            class_id INTEGER REFERENCES classrooms(id),
                                            course_length INTEGER NOT NULL
                                        )"""
    sql_create_students = """CREATE TABLE students (
                                            grade TEXT PRIMARY KEY,
                                            count INTEGER NOT NULL
                                        ) """
    sql_create_classrooms = """CREATE TABLE classrooms (
                                            id INTEGER PRIMARY KEY,
                                            location TEXT NOT NULL,
                                            current_course_id INTEGER NOT NULL,
                                            current_course_time_left INTEGER NOT NULL
                                        ) """

    connection_object = sqlite3.connect(CONST_DB_NAME)
    cursor_object = connection_object.cursor()
    if connection_object is not None:
        cursor_object.execute(sql_create_courses)
        cursor_object.execute(sql_create_students)
        cursor_object.execute(sql_create_classrooms)
        add_to_tables(cursor_object)
        print_tables(cursor_object)
    connection_object.commit()
    cursor_object.close()
    connection_object.close()


def main():
    if len(sys.argv) != 2:
        print("Please insert configuration file")
    elif not(os.path.isfile(CONST_DB_NAME)):
        create_db()
    else:
        print(CONST_DB_NAME + " already exists")


if __name__ == "__main__":
    main()
