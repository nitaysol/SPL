import os.path
import sqlite3
CONST_DB_NAME = "schedule.db"
CONST_RESULT_DICT = {
    1: "is schedule to start",
    2: "occupied by",
    3: "is done"
}


def print_change(case_to_print, iteration_num, classroom, course_name):
    if case_to_print == 1 or case_to_print == 3:
        print("({}) {}: {} {}".format(iteration_num, classroom[1], course_name, CONST_RESULT_DICT[case_to_print]))
    else:
        print("({}) {}: {} {}".format(iteration_num, classroom[1], CONST_RESULT_DICT[case_to_print], course_name, ))


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


def assign_students_to_class(cursor_object, connection_object, course_capacity, course_students):
    students_number = cursor_object.execute("""SELECT * FROM students WHERE grade = ?""", (course_students, )).fetchall()
    new_capacity = students_number[0][1] - course_capacity
    if new_capacity < 0:
        new_capacity = 0
    cursor_object.execute("""UPDATE students SET count = ? WHERE grade = ?""", (new_capacity, course_students, ))
    connection_object.commit()


def schedule_simulation(cursor_object, iteration_num, connection_object):
    classrooms = cursor_object.execute("""SELECT * FROM classrooms""").fetchall()
    for classroom in classrooms:
        if classroom[3] == 0 or classroom[3] == 1:
            if classroom[2] != 0 and classroom[3] == 1:
                course_name = cursor_object.execute("""SELECT * FROM courses WHERE id = ?""",
                                                    (classroom[2], )).fetchone()[1]

                cursor_object.execute("""DELETE FROM courses WHERE id = ?""", (classroom[2], ))
                cursor_object.execute("""UPDATE classrooms SET current_course_id=0,
                                current_course_time_left=0 WHERE id = ?""", (classroom[0], ))
                connection_object.commit()
                print_change(3, iteration_num, classroom, course_name)

            course_to_enter = cursor_object.execute("""SELECT * FROM courses WHERE class_id = ?"""
                                                    , (classroom[0], )).fetchone()
            if course_to_enter is not None:
                cursor_object.execute("""UPDATE classrooms SET current_course_id = ?,
                current_course_time_left = ? WHERE id = ?""", (course_to_enter[0], course_to_enter[5], classroom[0], ))
                connection_object.commit()
                print_change(1, iteration_num, classroom, course_to_enter[1])
                assign_students_to_class(cursor_object, connection_object,
                                         course_to_enter[3], course_to_enter[2])
        else:
            course_name = cursor_object.execute("""SELECT * FROM courses WHERE id = ?""", (classroom[2], )).fetchone()[1]
            cursor_object.execute("""UPDATE classrooms SET current_course_time_left = ? WHERE id = ?""",
                                  (classroom[3]-1, classroom[0], ))
            connection_object.commit()
            print_change(2, iteration_num, classroom, course_name)

    print_tables(cursor_object)


def check_courses_is_empty(cursor_object):
    return len(cursor_object.execute("""SELECT * FROM courses""").fetchall()) == 0


def connect_to_db():
    connection_object = sqlite3.connect(CONST_DB_NAME)
    cursor_object = connection_object.cursor()
    return cursor_object, connection_object


def main():
    iteration_num = 0
    should_terminate = False;
    cursor_object = None
    while not should_terminate:
        if not os.path.isfile(CONST_DB_NAME):
            print(CONST_DB_NAME + " not found")
            should_terminate = True
        else:
            if cursor_object is None:
                cursor_object, connection_object = connect_to_db()
            if check_courses_is_empty(cursor_object):
                should_terminate = True
                if iteration_num == 0:
                    print_tables(cursor_object)
                cursor_object.close()
                connection_object.close
            else:
                schedule_simulation(cursor_object, iteration_num, connection_object)
        iteration_num = iteration_num + 1


if __name__ == "__main__":
    main()