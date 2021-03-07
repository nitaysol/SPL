#include "../include/Restaurant.h"
#include <iostream>
#include <fstream>
#include <sstream>
#include <string>

using namespace std;

Restaurant::Restaurant():
customersCounter(0),
open(true),
tables(),
menu(),
actionsLog()
{}
Restaurant::Restaurant(const std::string &configFilePath):
        customersCounter(0),
open(true),
tables(),
menu(),
actionsLog()
{
    std::ifstream file(configFilePath);
    if (file.is_open()) {
        int dishCounter(0);
        string line;
        //scenario will determine on which part of the config we are working(tables / menu)
        int scenario(1);
        //ignore empty lines and comments
        while (getline(file, line)) {
            if (!(line.find('#')==0 || line.empty()))
            {
                switch (scenario) {
                    //number of tables
                    case 1 : {
                        tables.reserve((unsigned long)stoi(line));
                        scenario=2;
                        break;
                    }
                    //tables capacity
                    case 2 : {
                        stringstream ss(line);
                        int i;
                        while (ss >> i) {
                            Table* t = new Table(i);
                            tables.push_back(t);
                            if (ss.peek() == ',')
                                ss.ignore();
                        }
                        scenario = 3;
                        break;
                    }
                    //dishes
                    case 3 : {
                        //position of first and second comma's in order to split the string to dishName&Type
                        size_t pos = line.find_first_of(',');
                        size_t pos2 = line.substr(pos+1).find_first_of(',');
                        string dishName = line.substr(0, pos);
                        string dishTypeStr = line.substr(pos+1, pos2);
                        int dishPrice = stoi(line.substr(pos + pos2 + 2));
                        DishType dishType=ALC;
                        //Moving on all possibilities - assuming valid input.
                        if(dishTypeStr=="ALC") {
                            dishType = ALC;
                        }
                        else if(dishTypeStr=="VEG") {
                            dishType = VEG;
                        }
                        else if(dishTypeStr=="BVG") {
                            dishType = BVG;
                        }
                        else if(dishTypeStr=="SPC") {
                            dishType = SPC;
                        }
                        Dish d(dishCounter, dishName, dishPrice, dishType);
                        dishCounter++;
                        menu.push_back(d);
                        break;
                    }
                    default: break;
                }
            }
        }
        file.close();
    }
    else{ cout << "cant open file" <<endl;}
}
Table* Restaurant::getTable(int ind) {
    if(getNumOfTables()<=ind) return nullptr;
    return tables[ind];
}
std::vector<Dish>& Restaurant::getMenu() {
    return menu;
}
const std::vector<BaseAction*>& Restaurant::getActionsLog() const {
    return actionsLog;
}
void Restaurant::start() {
    std::string command;
    open=true;
    cout << "Restaurant is now open!" << endl;
    std::getline(std::cin, command);
    BaseAction* action;
    string input=command;
    while(command!="closeall")
    {
        input = command;
        //----------------------------------OPEN----------------------------------
        if (command.substr(0, command.find_first_of(" ")) == "open"){
            //Splitting the given string
            std::vector<Customer *> v;
            size_t found = command.find(" ");
            //command(which is obviously open(valid input is insured)
            command = command.substr(found + 1);
            //id of table
            int id = stoi(command.substr(0, command.find(" ")));
            command = command.substr(command.find(" ")+1);
            //Running on next string and separate each customer&type to variables.
            stringstream ss(command);
            string i;
            while (ss >> i) {
                string name(i.substr(0, i.find(',')));
                string type(i.substr(i.find(',')+1));
                if(type=="veg")
                {
                    VegetarianCustomer* c = new VegetarianCustomer(name, customersCounter);
                    v.push_back(c);
                }
                else if(type=="alc")
                {
                    AlchoholicCustomer* c = new AlchoholicCustomer(name, customersCounter);
                    v.push_back(c);
                }
                else if(type=="chp")
                {
                    CheapCustomer* c = new CheapCustomer(name, customersCounter);
                    v.push_back(c);
                }
                else if(type=="spc")
                {
                    SpicyCustomer* c = new SpicyCustomer(name, customersCounter);
                    v.push_back(c);
                }
                customersCounter=customersCounter+1;
                if (ss.peek() == ' ')
                    ss.ignore();
            }
            action = new OpenTable(id, v);

        }
        //----------------------------------Order----------------------------------
        else if (command.substr(0, command.find_first_of(" ")) == "order") {
            size_t found = command.find(" ");
            command = command.substr(found + 1);
            int id = stoi(command.substr(0, command.find(" ")));
            action = new Order(id);

        }
        //----------------------------------Close----------------------------------
        else if(command.substr(0, command.find_first_of(" ")) == "close")
        {
            size_t found = command.find(" ");
            command = command.substr(found + 1);
            int id = stoi(command.substr(0, command.find(" ")));
            action = new Close(id);
        }
        //----------------------------------Menu----------------------------------
        else if(command.substr(0, command.find_first_of(" ")) == "menu")
        {
            action = new PrintMenu();
        }
        //----------------------------------Log----------------------------------
        else if(command.substr(0, command.find_first_of(" ")) == "log")
        {
            action = new PrintActionsLog();
        }
        //----------------------------------Status----------------------------------
        else if(command.substr(0, command.find_first_of(" ")) == "status")
        {
            size_t found = command.find(" ");
            command = command.substr(found + 1);
            int id = stoi(command.substr(0, command.find(" ")));
            action = new PrintTableStatus(id);

        }
        //----------------------------------Move----------------------------------
        else if(command.substr(0, command.find_first_of(" ")) == "move")
        {
            size_t found = command.find(" ");
            command = command.substr(found+1);
            int src = stoi(command.substr(0, command.find_first_of(" ")));
            command = command.substr(command.find_first_of(" ")+1);
            int dst = stoi(command.substr(0, command.find_first_of(" ")));
            command = command.substr(command.find_first_of(" ")+1);
            int id = stoi(command.substr(0, command.find_first_of(" ")));
            action = new MoveCustomer(src, dst, id);
        }
        //----------------------------------BackUp----------------------------------
        else if(command.substr(0, command.find_first_of(" ")) == "backup")
        {
            action= new BackupRestaurant();

        }
        else if(command.substr(0, command.find_first_of(" ")) == "restore")
        {
            action = new RestoreResturant();

        }
        action->act(*this);
        actionsLog.push_back(action);
        action->setInputMsg(input);

        //get next input
        std::getline(std::cin, command);

    }
    //if input is close all
    action = new CloseAll();
    action->act(*this);
    action->setInputMsg(input);
    actionsLog.push_back(action);
}
int Restaurant::getNumOfTables() const {return (int)tables.size();}

//----------------------------------Rule Of 5----------------------------------
//----------------------------------Copy Constructor----------------------------------
Restaurant::Restaurant(const Restaurant &other):customersCounter(other.customersCounter),open(other.open),tables(),menu(other.menu),actionsLog()
{
    for(size_t i=0;i<other.getActionsLog().size(); i++)
    {
        actionsLog.push_back(other.getActionsLog().at(i)->Clone());
    }
    for(int i=0;i<other.getNumOfTables(); i++)
    {
        tables.push_back(other.tables.at(i)->Clone());
    }
}
//clear is clearing all data in restaurant
void Restaurant::clear(){
    for (auto it = tables.begin() ; it != tables.end(); ++it)
    {
        delete *it;
    }
    for (size_t i=0; i<actionsLog.size();i++)
    {
        delete actionsLog.at(i);
    }

    this->actionsLog.clear();
    this->tables.clear();
    this->menu.clear();
}

//----------------------------------Destructor----------------------------------
Restaurant::~Restaurant() {
    this->clear();
}
//----------------------------------Copy operator----------------------------------
Restaurant& Restaurant::operator=(const Restaurant &other) {
    if(this == &other)
        return *this;
    this->clear();
    for(auto it = other.menu.begin() ; it != other.menu.end(); ++it)
    {
        menu.push_back(*it);
    }
    open=other.open;
    customersCounter=other.customersCounter;
    for(size_t i=0; i<other.tables.size(); i++){
        tables.push_back(other.tables[i]->Clone());
    }
    for (size_t i=0 ; i<other.actionsLog.size() ; i++)
    {
        actionsLog.push_back(other.actionsLog[i]->Clone());
    }
    return *this;
}
//----------------------------------Move constructor----------------------------------
Restaurant::Restaurant(Restaurant&& other):
customersCounter(0),
open(),
tables(std::move(other.tables)),
menu(),
actionsLog()
{
    open=other.open;
    customersCounter=other.customersCounter;
    actionsLog = other.actionsLog;
    other.actionsLog.clear();
    for(auto it = other.menu.begin() ; it != other.menu.end(); ++it)
    {
        menu.push_back(*it);
    }
    other.menu.clear();
}
//----------------------------------Move assignment operator----------------------------------
Restaurant& Restaurant::operator=(Restaurant &&other)
{
    clear();
    tables = other.tables;
    other.tables.clear();
    open=other.open;
    customersCounter=other.customersCounter;
    actionsLog = other.actionsLog;
    other.actionsLog.clear();
    for(auto it = other.menu.begin() ; it != other.menu.end(); ++it)
    {
        menu.push_back(*it);
    }
    other.menu.clear();
    return *this;
}
