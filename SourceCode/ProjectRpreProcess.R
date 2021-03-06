library(data.table)
library(Matrix)
library(caret)
library(xgboost)

dt <- fread("F:/UTD/Sem 4/Machine Learning/Project/train_numeric.csv",
            drop = "Id",
            nrows = 6000,
            showProgress = F)

Y  <- dt$Response
dt[ , Response := NULL]

for(col in names(dt)) set(dt, j = col, value = dt[[col]] + 2)
for(col in names(dt)) set(dt, which(is.na(dt[[col]])), col, 0)

X <- Matrix(as.matrix(dt), sparse = T)
rm(dt)

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

impFeatures = imp[imp$Gain>0.001]$Feature

######################################################################

mc <- function(actual, predicted) {
  
  tp <- as.numeric(sum(actual == 1 & predicted == 1))
  tn <- as.numeric(sum(actual == 0 & predicted == 0))
  fp <- as.numeric(sum(actual == 0 & predicted == 1))
  fn <- as.numeric(sum(actual == 1 & predicted == 0))
  
  numer <- (tp * tn) - (fp * fn)
  denom <- ((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn)) ^ 0.5
  
  numer / denom
}

matt <- data.table(thresh = seq(0.990, 0.999, by = 0.001))

matt$scores <- sapply(matt$thresh, FUN =
                        function(x) mc(Y[valid], (pred > quantile(pred, x)) * 1))

best <- matt$thresh[which(matt$scores == max(matt$scores))]


################################################################################
#looking at the test data
dt  <- fread("F:/UTD/Sem 4/Machine Learning/Project/train_numeric_full.csv",
             nrows = 50000,
             showProgress = F)

Id  <- dt$Id
dt[ , Id := NULL]

Y <- dt$Response 

for(col in names(dt)) set(dt, j = col, value = dt[[col]] + 2)
for(col in names(dt)) set(dt, which(is.na(dt[[col]])), col, 0)

X <- Matrix(as.matrix(dt), sparse = T)
rm(dt)

dtest <- xgb.DMatrix(X)
pred  <- predict(m1, dtest)

summary(pred)
#####################################################################

#######################################################################
#printing the result
ResponseLabels = (pred > quantile(pred, best))
sub   <- data.table(Id = Id,ResponseLabels)

write.csv(sub, "sub.csv", row.names = F)

accuracy = mean(ResponseLabels == Y)*100
print(accuracy)


