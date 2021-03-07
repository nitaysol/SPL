#include "../include/Action.h"
#include "iostream"
#include "../include/Restaurant.h"
extern Restaurant* backup;

using namespace std;
//----------------------------------BaseAction---------------------------------
//Constructor
BaseAction::BaseAction():
        errorMsg(),
        status(PENDING),
        input()
{}
//Methods:
BaseAction::~BaseAction() {}
ActionStatus BaseAction::getStatus() const {return status;}
std::string BaseAction::getErrorMsg() const {return errorMsg;}
void BaseAction::complete() {status=COMPLETED;}
void BaseAction::error(std::string errorMsg) {this->errorMsg=errorMsg; status=ERROR;}
std::string BaseAction::getInputMsg() {return input;}
void BaseAction::setInputMsg(std::string inputMsg) {
    if(status==COMPLETED)
    {
        input = inputMsg + " Completed";
    }
    else
        {
        input = inputMsg + " " + "Error: " + errorMsg;
    }
}

//----------------------------------OpenTable---------------------------------
//Constructor
OpenTable::OpenTable(int id, std::vector<Customer *> &customersList):
        BaseAction(),
        tableId(id),
        customers(customersList)
{}
//Methods:
void OpenTable::act(Restaurant &restaurant) {
    Table* table = restaurant.getTable(tableId);
    //first 2 if's ->if table isnt standing with the defined rules, delete it's customers list - preventing memory leaks.
    if(table==nullptr || table->isOpen())
    {

        for(size_t i=0; i<customers.size();i++)
        {
            delete customers[i];
        }
        error("Table does not exist or is already open");
        cout << "Error: " + getErrorMsg() << endl;
    }
    else if(table->getCapacity()<signed(customers.size()))
    {
        for(size_t i=0; i<customers.size();i++)
        {
            delete customers[i];
        }
        error("No capacity");
        cout << getErrorMsg() << endl;
    }
    //if valid:
    else{
        for(std::vector<Customer *>::const_iterator it=customers.begin();it!=customers.end();++it)
        {
            table->addCustomer(*it);
        }
        table->openTable();
        complete();
    }

}
std::string OpenTable::toString() const {
    return "table " + std::to_string(tableId) + " opened";
}


//----------------------------------Order----------------------------------
//Constructor
Order::Order(int id):BaseAction(),tableId(id) {}
//Methods:
void Order::act(Restaurant &restaurant)
{
    if(restaurant.getNumOfTables()<tableId||!restaurant.getTable(tableId)->isOpen())
    {
        error("Table does not exist or is not open");
        cout << "Error: "+getErrorMsg() << endl;
    }
    else {
        restaurant.getTable(tableId)->order(restaurant.getMenu());
        complete();
    }
}
std::string Order::toString() const {return "Order Completed";}


//----------------------------------Close----------------------------------
//Constructor
Close::Close(int id):
        BaseAction(),
        tableId(id)
{}
//Methods:
void Close::act(Restaurant &restaurant) {
    Table* table = restaurant.getTable(tableId);
    int bill=0;
    if(table==nullptr || !table->isOpen())
    {
        error("Table does not exist or is not open");
        cout << "Error: " + getErrorMsg() << endl;
    }
    else{
        bill = table->getBill();
        table->closeTable();
        complete();
        cout << "Table " + to_string(tableId) + " was closed. Bill " + std::to_string(bill) +"NIS"<<endl;
    }
}
std::string Close::toString() const {return "table " + std::to_string(tableId) + " was closed.";}

//----------------------------------CloseAll----------------------------------
//Constructor
CloseAll::CloseAll():
        BaseAction()
{}
//Methods:
std::string CloseAll::toString() const {return "CloseAll";}
void CloseAll::act(Restaurant &restaurant) {
    for(int i=0; i<restaurant.getNumOfTables(); i++)
    {
        if(restaurant.getTable(i)->isOpen())
        {
            Close c(i);
            c.act(restaurant);
        }
    }
    complete();
}

//----------------------------------PrintActionLog----------------------------------
//Constructor
PrintActionsLog::PrintActionsLog():
        BaseAction()
{}
//Methods:
std::string PrintActionsLog::toString() const {return "Action LOG:";}
void PrintActionsLog::act(Restaurant &restaurant) {
    std::vector<BaseAction*> vActions(restaurant.getActionsLog());
    for(auto it=vActions.begin();it!=vActions.end();it++)
    {

        cout<<(*it)->getInputMsg() <<endl;
    }
    complete();
}


//----------------------------------Backup----------------------------------
//Constructor
BackupRestaurant::BackupRestaurant():
        BaseAction()
{}
//Methods:
std::string BackupRestaurant::toString() const {return "Backup Restaurant";}
void BackupRestaurant::act(Restaurant &restaurant) {
    if(backup== nullptr) {
        backup = new Restaurant(restaurant);
    } else{
        *backup = restaurant;
    }

    complete();
    }

//----------------------------------Restore----------------------------------
//Constructor
RestoreResturant::RestoreResturant():
        BaseAction()
{}
//Methods:
std::string RestoreResturant::toString() const {return "Restore Restaurant";}
void RestoreResturant::act(Restaurant &restaurant) {
    if(backup== nullptr) {
        error("No backup available");
        cout<<"Error: " + getErrorMsg()<< endl;
    }
    else{
        restaurant = *backup;
        complete();
    }
}



//----------------------------------PrintMenu----------------------------------
//Constructor
PrintMenu::PrintMenu():BaseAction() {}
//Methods:
std::string PrintMenu::toString() const {return "The Menu Printed";}
void PrintMenu::act(Restaurant &restaurant)
{
    string s;
    for(std::vector<Dish>::const_iterator it=restaurant.getMenu().begin();it!=restaurant.getMenu().end();++it)
    {
        if((*it).getType()==SPC)
            s="SPC";
        else if((*it).getType()==VEG)
            s="VEG";
        else if((*it).getType()==BVG)
            s="BVG";
        else
            s="ALC";
        std::cout << (*it).getName() + " "+s+" "+to_string((*it).getPrice())+"NIS"<<std::endl;
    }
    complete();
}

//----------------------------------PrintTableStatus----------------------------------
//Constructor
PrintTableStatus::PrintTableStatus(int id):BaseAction(),tableId(id) {}
//Methods:
std::string PrintTableStatus::toString() const {return "Table status Completed";}
void PrintTableStatus::act(Restaurant &restaurant)
{
    complete();
    string s("");
    int sum(0);
    if(restaurant.getTable(tableId)->isOpen())
    {
        std::cout <<  "Table "+std::to_string(tableId)+" status: open" <<std::endl;
        std::cout <<  "Customers:"<<std::endl;
        for(std::vector<Customer*>::iterator it=restaurant.getTable(tableId)->getCustomers().begin();it!=restaurant.getTable(tableId)->getCustomers().end();++it)
        {
            s=(*it)->toString();
            std::cout << s <<std::endl;
        }
        std::cout <<  "Orders:"<<std::endl;
        for(std::vector<OrderPair>::const_iterator it2=restaurant.getTable(tableId)->getOrders().begin();it2!=restaurant.getTable(tableId)->getOrders().end();++it2)
        {
            std::cout <<  (*it2).second.getName()+" "+to_string((*it2).second.getPrice())+"NIS "+std::to_string((*it2).first)<<std::endl;
            sum+=(*it2).second.getPrice();
        }
        std::cout <<  "Current Bill: "+std::to_string(sum)+"NIS"<<std::endl;
    }
    else
        std::cout <<  "Table "+std::to_string(tableId)+" status: closed"<<std::endl;

}

//----------------------------------Move Customer----------------------------------
//Constructor
MoveCustomer::MoveCustomer(int src, int dst, int customerId):
    BaseAction(),
    srcTable(src),
    dstTable(dst),
    id(customerId)

{}
//Methods:
std::string MoveCustomer::toString() const {return "Moving Customer";}
void MoveCustomer::act(Restaurant &restaurant)
{
    //Setting errors in the beginning - if success we'll change it in the end.
    error("Cannot move customer");
    string err("Error: Cannot move customer");
    //first 3 if's checking validity
    if(restaurant.getNumOfTables()<dstTable||restaurant.getNumOfTables()<srcTable||(!restaurant.getTable(srcTable)->isOpen()||!restaurant.getTable(dstTable)->isOpen()))
    {
        std::cout <<  err<<std::endl;
    }
    else if(signed(restaurant.getTable(dstTable)->getCustomers().size())==restaurant.getTable(dstTable)->getCapacity())
        std::cout <<  err<<std::endl;
    else if(restaurant.getTable(srcTable)->getCustomer(id)==nullptr)
        std::cout <<  err<<std::endl;
    //if valid:
    else
    {
        //Moving this customer orders to the dst table.
        for(std::vector<OrderPair>::iterator it=restaurant.getTable(srcTable)->getOrders().begin();it!=restaurant.getTable(srcTable)->getOrders().end();++it)
        {
            if((*it).first==id)
                restaurant.getTable(dstTable)->getOrders().push_back((*it));
        }
        //Adding the customer to dst table
        restaurant.getTable(dstTable)->addCustomer(restaurant.getTable(srcTable)->getCustomer(id));
        //Remove customer from src table including his orders.
        restaurant.getTable(srcTable)->SetOrdersAfterMove(id);
        if(restaurant.getTable(srcTable)->getCustomers().size()==0)
        {
            restaurant.getTable(srcTable)->closeTable();
        }
        error("");
        complete();
    }
}

//----------------------------------Clones----------------------------------
PrintMenu* PrintMenu::Clone(){ return new PrintMenu(*this); };
RestoreResturant* RestoreResturant::Clone(){ return new RestoreResturant(*this); };
BackupRestaurant* BackupRestaurant::Clone(){ return new BackupRestaurant(*this); };
PrintActionsLog* PrintActionsLog::Clone(){ return new PrintActionsLog(*this); };
PrintTableStatus* PrintTableStatus::Clone(){ return new PrintTableStatus(*this); };
CloseAll* CloseAll::Clone(){ return new CloseAll(*this); };
Close* Close::Clone(){ return new Close(*this); };
MoveCustomer* MoveCustomer::Clone(){ return new MoveCustomer(*this); };
Order* Order::Clone(){ return new Order(*this); };
OpenTable* OpenTable::Clone(){ return new OpenTable(*this); };
