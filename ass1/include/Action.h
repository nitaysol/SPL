#ifndef ACTION_H_
#define ACTION_H_

#include <string>
#include <iostream>
#include "Customer.h"

enum ActionStatus{
    PENDING, COMPLETED, ERROR
};

//Forward declaration
class Restaurant;

class BaseAction{
public:
    BaseAction();
    virtual ~BaseAction();
    ActionStatus getStatus() const;
    virtual void act(Restaurant& restaurant)=0;
    virtual std::string toString() const=0;
    virtual BaseAction* Clone()=0;
    std::string getInputMsg();
    void setInputMsg(std::string inputMsg);
protected:
    void complete();
    void error(std::string errorMsg);
    std::string getErrorMsg() const;

private:
    std::string errorMsg;
    ActionStatus status;
    std::string input;
};


class OpenTable : public BaseAction {
public:
    OpenTable(int id, std::vector<Customer *> &customersList);
    virtual ~OpenTable()= default;
    void act(Restaurant &restaurant);
    std::string toString() const;
    OpenTable* Clone();
private:
	const int tableId;
	std::vector<Customer *> customers;
};


class Order : public BaseAction {
public:
    Order(int id);
    virtual ~Order()= default;
    void act(Restaurant &restaurant);
    std::string toString() const;
    Order* Clone();
private:
    const int tableId;
};


class MoveCustomer : public BaseAction {
public:
    MoveCustomer(int src, int dst, int customerId);
    virtual ~MoveCustomer()= default;
    void act(Restaurant &restaurant);
    std::string toString() const;
    MoveCustomer* Clone();
private:
    const int srcTable;
    const int dstTable;
    const int id;
};


class Close : public BaseAction {
public:
    Close(int id);
    virtual ~Close()= default;
    void act(Restaurant &restaurant);
    std::string toString() const;
    Close* Clone();
private:
    const int tableId;
};


class CloseAll : public BaseAction {
public:
    CloseAll();
    virtual ~CloseAll()= default;
    void act(Restaurant &restaurant);
    std::string toString() const;
    CloseAll* Clone();
private:
};


class PrintMenu : public BaseAction {
public:
    PrintMenu();
    virtual ~PrintMenu()= default;
    void act(Restaurant &restaurant);
    std::string toString() const;
    PrintMenu* Clone();
private:
};


class PrintTableStatus : public BaseAction {
public:
    virtual ~PrintTableStatus()= default;
    PrintTableStatus* Clone();
    PrintTableStatus(int id);
    void act(Restaurant &restaurant);
    std::string toString() const;
private:
    const int tableId;
};


class PrintActionsLog : public BaseAction {
public:
    virtual ~PrintActionsLog()= default;
    PrintActionsLog* Clone();
    PrintActionsLog();
    void act(Restaurant &restaurant);
    std::string toString() const;
private:
};


class BackupRestaurant : public BaseAction {
public:
    virtual ~BackupRestaurant()= default;
    BackupRestaurant*Clone();
    BackupRestaurant();
    void act(Restaurant &restaurant);
    std::string toString() const;
private:
};


class RestoreResturant : public BaseAction {
public:
    virtual ~RestoreResturant()= default;
    RestoreResturant* Clone();
    RestoreResturant();
    void act(Restaurant &restaurant);
    std::string toString() const;

};


#endif
