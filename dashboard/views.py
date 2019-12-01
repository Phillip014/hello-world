from django.shortcuts import render, redirect
from django.http import HttpResponse
from .forms import SearchForm
from django.urls import reverse
import re
import time

# Create your views here.
def index(request):
    return HttpResponse("Hello world!")

def index1(request):##we do not use this function
    context = dict()
    if request.method == "POST":
        form = SearchForm(request.POST)
        if form.is_valid():
            post_data = form.cleaned_data['search']
            print(post_data)
            context['content'] = "hi"+post_data
        return render(request,'result.html',context)
    else:
        form = SearchForm()
    context['form'] = form
    return render(request, 'index.html', context)

def result(request):
    start = time.time()#recording the starting time
    context = dict()
    
    if request.method == "POST":
        form = SearchForm(request.POST)
        if form.is_valid():
            post_data = form.cleaned_data['search']
            context['content'] = post_data  #record the searching word
            filename = loadfilename(post_data,"static/sort.txt")#find the name of the file that has the searching word
            context['filename'] = filename
            
            if filename:
                lines=[]
                results=[]
                for i in filename:#traverse all of the filename and then append it into one list
                    tmp,line,nextline,count=loadsearchfile(post_data,'static/essay/{name}'.format(name = i))
                    
                    results.append([i,tmp,line,nextline,count])
                context['result'] = results  
            else:#if there are not such word ,then print there are not such word
                context['error'] = "No this word !"
                     
    else:
        form = SearchForm()
    context['form'] = form
    end = time.time()#recording the endding time
    context['time'] = "The searching time is {name}s".format(name = str(end-start)) #print the run time
    return render(request, 'result.html', context)



def loadfilename(question,filepath):
    fopen = open(filepath,"r")#load the tf-idf file
    lines = fopen.readlines()
    filename = []
    for line in lines:
        allthing = line.split("/")#split a line into three parts----keyword,filename and the value of tf-idf
        key = allthing[0]#extract the keyword
        if question == key:#if the keyword is equal to question, then that is the word we want to find
            answer = allthing[1]
            filename.append(answer)
        
    return filename

def loadsearchfile(question,filepath):
    linenum = 0
    count=[]
    fopen = open(filepath,"r")
    lines = fopen.readlines()
    tmp = ''
    for line in lines:#this is use to record all the lines number
        linenum = linenum + 1
        if (re.findall(question, line,flags = re.IGNORECASE)):
            count.append(linenum)
    linenum2 = 0
    for line in lines:# this is use to record the front,current and back context that we want to find it.
        linenum2 = linenum2 + 1
        nextline = lines[linenum2]
        if (re.findall(question, line,flags = re.IGNORECASE)):
            return tmp, line, nextline,count[:3]
        tmp = line
            
    
    


