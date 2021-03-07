#include "../include/Table.h"
#include "iostream"
using namespace std;

//----------------------------------Table----------------------------------
//Constructor
Table::Table(int t_capacity):
    capacity(t_capacity),
    open(),
    customersList(),
    orderList()
{}
//Methods:
int Table::getCapacity() const {return capacity;}
bool Table::isOpen() {return open;}
std::vector<Customer*>& Table::getCustomers() {return customersList;}
std::vector<OrderPair>& Table::getOrders() {return orderList;}


void Table::addCustomer(Customer *customer) {
    if (signed(customersList.size())<capacity) {
        customersList.push_back (customer);
    }
}

void Table::removeCustomer(int id)
{
    bool found(false);
    for (auto it=customersList.begin();it!=customersList.end() && !found;it++)
    {
        if((*it)->getId()==id)
        {
            customersList.erase(it);
            found=true;
        }
    }
}

int Table::getBill()
{
    int sum=0;
    for(std::vector<OrderPair>::iterator it=orderList.begin();it!=orderList.end();++it)
    {
        sum+=(*it).second.getPrice();
    }
    return sum;
}


Customer* Table::getCustomer(int id)
{
    for(std::vector<Customer*>::iterator it=customersList.begin();it!=customersList.end();++it)
    {
        if((*it)->getId()==id)
            return (*it);
    }
    return nullptr;
}

    void Table::openTable() {open=true;}

    void Table::closeTable() {open=false;
    orderList.clear();
    for (size_t i=0;i<customersList.size();i++)
    {
        delete customersList[i];
        customersList[i] = nullptr;
    }
    customersList.clear();
}

void Table::order(const std::vector<Dish> &menu)
{
    vector<int> customerOrder;
    //Running on each customer and use its "method" of ordering
    for(std::vector<Customer*>::iterator it=customersList.begin();it!=customersList.end();++it)
    {
        customerOrder=(*it)->order(menu);
        for(std::vector<int>::iterator itCustomer=customerOrder.begin();itCustomer!=customerOrder.end();++itCustomer)
        {
            cout << ""+(*it)->getName()+" ordered "+ (menu.at(*itCustomer)).getName() <<endl;
            OrderPair p((*it)->getId(),menu.at(*itCustomer));
            orderList.push_back(p);
        }
    }
}
//Set order List after moving a customer from this table to another.
void Table::SetOrdersAfterMove(int customerID)
{
    vector<OrderPair> op;
    for(std::vector<OrderPair>::iterator it=orderList.begin();it!=orderList.end();++it)
    {
        //if the order doesnt belong to the customer add it to the vector(in the end we'll replace the orders vector with the new one)
        if(!((*it).first==customerID)) {
            op.push_back((*it));
        }
    }
    orderList.clear();
    for(auto it=op.begin();it!=op.end();it++)
    {
        orderList.push_back(*it);
    }
    removeCustomer(customerID);



}
//----------------------------------Rule Of 5----------------------------------
//----------------------------------Copy Constructor----------------------------------
Table::Table(const Table &other):
capacity(other.capacity),
open(other.open),
customersList(),
orderList(other.orderList)
{
    for(size_t i=0; i<other.customersList.size(); i++){
        this->customersList.push_back(other.customersList[i]->Clone());
    }
}

//----------------------------------Destructor----------------------------------
void Table::clear() {
    for(size_t i=0; i<customersList.size() ; i++){
        delete customersList[i];
        customersList[i] = nullptr;
    }
    orderList.clear();
    customersList.clear();
    open = false;
    capacity=0;
}
Table::~Table() {
clear();
}
//----------------------------------Copy operator----------------------------------
Table* Table::Clone(){
    return new Table(*this);
};
Table& Table::operator=(const Table &other) {
    if(&other==this) return *this;
    this->clear();
    open = other.open;
    capacity = other.capacity;
    for(size_t i=0; i<customersList.size(); i++){
        this->customersList.push_back(other.customersList[i]->Clone());
    }
    for(size_t i=0; i<other.orderList.size(); i++){
        this->orderList.push_back(other.orderList[i]);
    }
    return *this;
}

//----------------------------------Move constructor----------------------------------
Table::Table(Table &&other):
capacity(other.capacity),
open(other.open),
customersList(),
orderList(std::move(other.orderList))
{
    for(size_t i=0; i<other.customersList.size(); i++){
        this->customersList.push_back(other.customersList[i]);
    }
    other.orderList.clear();
    other.customersList.clear();
}
//----------------------------------Move assignement----------------------------------
Table& Table::operator=(Table &&other) {
    if(this==&other) return *this;
    clear();
    capacity=other.capacity;
    open=other.open;
    for(size_t i=0; i<other.customersList.size(); i++){
        this->customersList.push_back(other.customersList[i]);
    }
    other.customersList.clear();
    for(size_t i=0; i<other.orderList.size(); i++){
        this->orderList.push_back(other.orderList[i]);
    }
    other.orderList.clear();

    return *this;
}