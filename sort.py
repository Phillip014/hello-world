import pandas as pd
import numpy as py

test1 = pd.read_csv("newTF.txt", names=['a','b','c'],sep="\s+")
test2 = pd.DataFrame(test1)
test3 = test2.sort_values(by=['a','c'],ascending=(True,False))
test3.to_csv('newsort.txt',sep='/',header=False,index=False)