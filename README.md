# Personalized queries components

This project contains personalized queries components.

## Minimal requirements

* DX 7.2.2.0
* Marketing Factory 1.9.3

## Build and deploy

Simply use the following command to build the project and deploy to Jahia

```
mvn clean install jahia:deploy
```

## Installation
Download the jar and deploy it on your instance then activate the module on the site you wish to use it.

## Personalized News Retriever Component

The component used to retrieve the latest news according to the profile criteria and the entered parameters.

### How it works ?

The criteria used to retrieve the list of news is:

* The news has a date property and according to this property, we filter the list of news which has a date on the range of the number of last days set by the editor.
* The news already seen by the visitor will not be showing on the list.
* The highest tag aggregation of the profile will be used to get news tagged by the same tag.

The following diagram show the sequence of interaction between components to retrieve le latest news.

![sequence diagram of Personalized News Retriever](https://user-images.githubusercontent.com/8075371/42956916-7aa945cc-8b81-11e8-88ce-b969c43f2e42.png)

## Machine learning component

:rotating_light: Warning: this component is a proof of concept for one of innovation week topics. :rotating_light:

The main idea of the component is to collaborate with Apache Unomi and TensorFlow ML library to recommend articles on DX website.

### What is TensorFlow.js ?
[TensorFlow.js](https://js.tensorflow.org/) is a JavaScript library which makes it possible to add machine learning capabilities to any web application. With TensorFlow.js you can develop machine learning scenarios from scratch. You can use the APIs to build and train models right in the browser or in your Node.js server application. Furthermore you can use TensorFlow.js to run existing models in your JavaScript environment.

### Defining The Model

To ease start with machine learning, we have defined a simple scenario to recommend articles based on a mathematical formula:

>Total = (4 * NumberOfView) + (2 * NumberOfTags) + (NumberOfCartegories)

So the main idea is to educate/train our Model to solve this formula with some data after that we pass the real data we get from Apache Unomi to recommend to the end user the articles.

## Found a bug?

Please feel free to [create an issue](https://support.jahia.com/) if you find anything wrong with this component.
