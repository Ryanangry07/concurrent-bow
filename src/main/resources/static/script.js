document.addEventListener('DOMContentLoaded', function () {
    const customIgnoreInput = document.getElementById('custom-ignore-words');
    const ignoreOptions = document.querySelectorAll('input[name="ignore-option"]');
    const loadingOverlay = document.getElementById('loading-overlay');
    loadingOverlay.style.display = 'none';
    
    ignoreOptions.forEach(option => {
        option.addEventListener('change', function () {
            if (this.value === 'custom') {
                customIgnoreInput.disabled = false;
            } else {
                customIgnoreInput.disabled = true;
                customIgnoreInput.value = '';
            }
        });
    });


    document.getElementById('file-upload').addEventListener('change', function () {
        const files = document.getElementById('file-upload').files;
        const fileNames = [];
        for (const file of files) {
            fileNames.push(file.name);
        }
        console.log('Selected files:', fileNames);
    });

    document.getElementById('analyze-btn').addEventListener('click', function() {

        // check user input for analyze method

        const isAnyMethodSelected = document.querySelector('input[name="analysis-method"]:checked') != null;
        if (!isAnyMethodSelected) {
            alert('Please select at least one analysis method.');
            return;
        }

        console.log('testtest1');
        const files = document.getElementById('file-upload').files;
        //const selectedFiles = document.getElementById('file-upload').files;
        if (files.length === 0) {
            alert('Please select files to analyze.');
            return;
        }
        const ignoreOption = document.querySelector('input[name="ignore-option"]:checked') ?
            document.querySelector('input[name="ignore-option"]:checked').value : 'false';
        const customIgnoreWords = document.getElementById('custom-ignore-words') ?
            document.getElementById('custom-ignore-words').value.split(',').map(word => word.trim()) : 'false';

        //const customIgnoreWords = document.getElementById('custom-ignore-words').value.split(',').map(word => word.trim());
        const analysisMethod = Array.from(document.querySelectorAll('input[name="analysis-method"]:checked'))
            .map(input => input.value);
        
        loadingOverlay.style.display = 'flex';

        const formData = new FormData();
        for (const file of files) {
            formData.append('files', file);
        }
        formData.append('ignoreOption', ignoreOption);
        formData.append('customIgnoreWords', customIgnoreWords);
        formData.append('analysisMethod', analysisMethod);

        if(!customIgnoreWords){
            const input = document.getElementById('custom-ignore-words').value;
            const words = input.split(',').map(word => word.trim());
            formData.append('customIgnoreWords', words);
        }

        console.log('form-data: ');
        for (var pair of formData.entries()) {
            console.log(pair[0]+ ', ' + pair[1]);
        }

        fetch('/api/wordcount', {
            method: 'POST',
            body: formData
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            //displayResults(data);
            //sessionStorage.setItem('analysisResults', JSON.stringify(data.jsonResults));
            // sessionStorage.setItem('analysisResults', JSON.stringify(data));
            // console.log("index page: " + data.jsonResults);
            console.log("index page: " + response);
            loadingOverlay.style.display = 'none';
            window.location.href='../resultPage/results.html';
        })
        .catch(error => {
            console.error('Error processing files:', error);
            // document.getElementById('results-display').innerHTML = `<p>Error loading results.</p>`;
            loadingOverlay.style.display = 'none';
            alert('Error loading results.')
            return;
        });

        console.log('testtest');
        // window.location.href='../resultPage/results.html';

        /*fetch('/api/wordcount')
            .then(response => {
                // Check if the request was successful
                if (!response.ok) {
                    throw new Error('Network response was not ok ' + response.statusText);
                }
                // Parse the JSON data returned from the server
                return response.json();
            })
            .then(data => {
                // Work with the JSON data here
                console.log(data);
            })
            .catch(error => {
                // Handle any errors that occurred during the fetch
                console.error('There was a problem with the fetch operation:', error);
            });*/

    });
});
