library(data.table)
library(Matrix)
library(caret)
library(xgboost)
library(dplyr)
library(ggplot2)
library(plotly)
install.packages("tidyr")
library(reshape)
library(tidyr)
train_dat <- fread("D:/03Sem/ML/Project/Data/train_date.csv", nrows = 1)
train_num <- fread("D:/03Sem/ML/Project/Data/train_numeric.csv", nrows = 1)
train_cat <- fread("D:/03Sem/ML/Project/Data/test_categorical.csv", nrows = 1)
#Numerical training set features
features_num<-data.frame(features=names(train_num),Ftype="Numeric")  

#Date training set features
features_dat<-data.frame(features=names(train_dat),Ftype="Date")  

#Categorical training set features
features_cat<-data.frame(features=names(train_cat),Ftype="Categorical")  
features_dat
# combine all 

features_stat<-rbind(features_num[2:969,],features_dat[2:1157,],features_cat[2:2141,])
features_stat
# extract line, station, feature number from feature names
features_stat<-cbind(features_stat,colsplit(features_stat$features, split = "_", names = c("Line","Station","Fno"))) # reshape
#features_stat<-cbind(features_stat,separate(features_stat$features,col = names,into = c("Line","Station","Fno"))) # reshape


features_stat[,3:5]<-apply(features_stat[,3:5], 2, function(x) as.numeric(gsub("[LSFD]", "", x)))

glimpse(features_stat)
# arrange features with accending order of feature no
y<-features_stat%>%
  arrange(desc(-Fno))

glimpse(y)

# we can see feature are ordered num-date-num and so on...

feature_plot<-ggplot(data = features_stat,aes(x=Station))+
  geom_point(aes(y=Fno,colour=factor(Ftype),pch=factor(Ftype)),size=1.5)+
  geom_vline(xintercept = c(-0.5,23.5,25.5,28.5,51.5), color = "red", size=.5)+
  scale_x_continuous(name="Station No", breaks=seq(0,51,3))+
  ylab("Features number")+
  annotate("text", x=c(10,24.5,27,40), y=4200, label= c("Line:L1","L2","L3","L4"),color = "blue")

feature_plot
