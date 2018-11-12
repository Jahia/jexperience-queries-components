ml = {
    _inputs: [],
    _values: [],
    init: function (values) {
        ml._values = values;

        // Define a model for linear regression.
        const model = tf.sequential();
        model.add(tf.layers.dense({units: 1, inputShape: [3]}));

        // Prepare the model for training: Specify the loss and the optimizer.
        model.compile({loss: 'meanSquaredError', optimizer: 'sgd'});

        // Generate some synthetic data for training.
        // Prepare training data
        const xs = tf.tensor2d([
                [1, 1, 1],
                [2, 1, 1],
                [3, 1, 2],
                [4, 2, 1],
                [-1, 1, 2],
                [-1, 1, 1]
            ],
            [6, 3]);
        const ys = tf.tensor2d([7, 11, 16, 21, 0, -1], [6, 1]);

        // Train the model using the data.
        model.fit(xs, ys, {epochs: 100}).then(() => {
            // Use the model to do inference on a data point the model hasn't seen before:
            ml._convertToInputs(values);
            const preds = model.predict(tf.tensor2d(ml._inputs, [ml._inputs.length, 3])).dataSync();

            let average = 0;
            preds.forEach((pred, i) => {
                console.log(`x: ${i}, pred: ${Math.round(pred)}`);
                average += pred;
            });
            average /= preds.length;
            console.log(`avg: ${average}`);

            // Show result
            let divLoading = document.getElementById("loading");
            divLoading.style.display = 'none';

            let divResult = document.getElementById("result");

            for (const r of ml._convertPredictionToResult(Math.round(average))) {
                const h3Element = document.createElement('h3');
                const aElement = document.createElement('a');
                aElement.href = r.url;
                aElement.innerHTML = r.name;

                h3Element.appendChild(aElement);
                divResult.appendChild(h3Element);
            }

            divResult.style.display = 'block';
        });
    },

    _convertToInputs: function (values) {
        for (const v of values) {
            ml._inputs.push([v.nView, v.nTags, v.nCategories]);
        }

        console.log(ml._inputs);
    },

    _convertPredictionToResult : function (average) {
        let result = [];

        for (const v of ml._values) {
            if (v.nTotal >= average) {
                result.push(v);
            }
        }

        return result;
    }
};
