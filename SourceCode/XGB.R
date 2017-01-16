library(data.table)
library(Matrix)
library(caret)
library(xgboost)

datanum <- fread("D:/03Sem/ML/Project/Data/train_numeric.csv",
            drop = "Id",
            nrows = 200000,
            showProgress = F)

Y  <- datanum$Response
datanum[ , Response := NULL]

for(col in names(datanum)) set(datanum, j = col, value = datanum[[col]] + 2)
for(col in names(datanum)) set(datanum, which(is.na(datanum[[col]])), col, 0)

X <- Matrix(as.matrix(datanum), sparse = T)
rm(datanum)

folds <- createFolds(as.factor(Y), k = 6)
valid <- folds$Fold1
model <- c(1:length(Y))[-valid]

param <- list(objective = "binary:logistic",
              eval_metric = "auc",
              eta = 0.01,
              base_score = 0.005,
              col_sample = 0.5) 

dmodel <- xgb.DMatrix(X[model,], label = Y[model])
dvalid <- xgb.DMatrix(X[valid,], label = Y[valid])

m1 <- xgb.train(data = dmodel, param, nrounds = 20,
                watchlist = list(mod = dmodel, val = dvalid))
#####################################################################

pred <- predict(m1, dvalid)

summary(pred)
###############################################################################

imp <- xgb.importance(model = m1, feature_names = colnames(X))

head(imp, 30)

important_Features = imp[imp$Gain>0.001]$Feature

######################################################################
### Gauge Matthews Coefficient

#Try a few thresholds and select best for Matthews Coefficient.

mc <- function(actual, predicted) {
  
  tp <- as.numeric(sum(actual == 1 & predicted == 1))
  tn <- as.numeric(sum(actual == 0 & predicted == 0))
  fp <- as.numeric(sum(actual == 0 & predicted == 1))
  fn <- as.numeric(sum(actual == 1 & predicted == 0))
  
  numer <- (tp * tn) - (fp * fn)
  denominator <- ((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn)) ^ 0.5
  
  numer / denominator
}

matt <- data.table(thresh = seq(0.990, 0.999, by = 0.001))

matt$scores <- sapply(matt$thresh, FUN =
                        function(x) mc(Y[valid], (pred > quantile(pred, x)) * 1))

mattbest <- matt$thresh[which(matt$scores == max(matt$scores))]


################################################################################
#looking at the test data
datanum  <- fread("D:/03Sem/ML/Project/Data/train_numeric.csv",
             nrows = 200000,
             showProgress = F)

Id  <- datanum$Id
datanum[ , Id := NULL]

Y <- datanum$Response 

for(col in names(datanum)) set(datanum, j = col, value = datanum[[col]] + 2)
for(col in names(datanum)) set(datanum, which(is.na(datanum[[col]])), col, 0)

X <- Matrix(as.matrix(datanum), sparse = T)
rm(datanum)

datanumest <- xgb.DMatrix(X)
pred  <- predict(m1, datanumest)

summary(pred)
#####################################################################

#######################################################################
#printing the result
ResponseLabels = (pred > quantile(pred, mattbest))
sub   <- data.table(Id = Id,ResponseLabels)

####Final result captured in sub.csv###
write.csv(sub, "sub.csv", row.names = F)
###FInding Accuracy####
accuracy = mean(ResponseLabels == Y)*100
print(accuracy)




