document.getElementById('loadAllResults').addEventListener('click', function() {
    fetch('/api/response.json')
        .then(response => response.json())
        .then(data => {
            const sortedResults = sortWordCounts(data.results);
            console.log(sortedResults);
            displayResults2(sortedResults);
            displayIgnoredWords(data.ignored);
            displayError(data.error);

            document.getElementById('loadAllResults').disabled = true;
            document.getElementById('loadAllResults').classList.add('disabled-button');

            document.getElementById('loadResultsSparately').disabled = true;
            document.getElementById('loadResultsSparately').classList.add('disabled-button');
        })
        .catch(error => {
            console.error('Error fetching data: ', error);
            document.getElementById('errorSection').innerHTML = `<p>Error loading results.</p>`;
        });
});

document.getElementById('loadResultsSparately').addEventListener('click', function() {
    fetch('/api/response.json')
        .then(response => response.json())
        .then(data => {
            const sortedResults = sortWordCounts(data.results);
            console.log(sortedResults)
            displayResults3(sortedResults);
            displayIgnoredWords(data.ignored);
            displayError(data.error);
            // drawWordCloud(data.results.sequential.wordCount);
            document.getElementById('loadAllResults').disabled = true;
            document.getElementById('loadAllResults').classList.add('disabled-button');

            document.getElementById('loadResultsSparately').disabled = true;
            document.getElementById('loadResultsSparately').classList.add('disabled-button');
        })
        .catch(error => {
            console.error('Error fetching data: ', error);
            document.getElementById('errorSection').innerHTML = `<p>Error loading results.</p>`;
        });
});

function sortWordCounts(results) {
    let sortedResults = [];
    for (let key in results) {
        let algorithmData = { ...results[key] };
        let wordCountArray = Object.entries(algorithmData.wordCount)
            .sort((a, b) => b[1] - a[1]);
        algorithmData.wordCount = wordCountArray;
        sortedResults.push({ key, value: algorithmData });
    }
    return sortedResults;
}

function displayResults2(results) {
    const columns = results.map(item => {
        const key = item.key;
        const value = item.value;
        if (!value || !value.algoTimeInMs || !value.totalTimeInMs || !value.totalWords || !value.wordCount) {
            console.error(`Missing data for ${key}`);
            return '';
        }

        let html = `
            <div class="result-column">
                <button class="toggle-button" aria-expanded="false">${escapeHTML(key)}</button>
                <div class="result-content" style="display: block;">
                    <p><strong>Algorithm Time: </strong>${escapeHTML(value.algoTimeInMs.toString())} ms</p>
                    <p><strong>Total Time Taken: </strong>${escapeHTML(value.totalTimeInMs.toString())} ms</p>
                    <p><strong>Total Words: </strong>${escapeHTML(value.totalWords.toString())}</p>
                    <table>
                        <tr>
                            <th>Word</th>
                            <th>Count</th>
                        </tr>
        `;
        value.wordCount.forEach(([word, count]) => {
            html += `<tr><td>${escapeHTML(word)}</td><td>${escapeHTML(count.toString())}</td></tr>`;
        });
        html += `</table></div></div>`;
        return html;
    }).join('');

    document.getElementById('results').innerHTML = `<div class="results-grid">${columns}</div>`;
    addToggleEventListeners();
}

function displayResults3(results) {
    const columns = results.map(item => {
        const key = item.key;
        const value = item.value;
        if (!value || !value.algoTimeInMs || !value.totalTimeInMs || !value.totalWords || !value.wordCount) {
            console.error(`Missing data for ${key}`);
            return '';
        }

        let html = `
            <div class="result-column">
                <button class="toggle-button" data-key="${escapeHTML(key)}" aria-expanded="false">${escapeHTML(key)}</button>
                <div class="result-content" style="display: none;" id="content-${escapeHTML(key)}">
                    <p>Algorithm Time: ${escapeHTML(value.algoTimeInMs.toString())} ms</p>
                    <p>Total Time Taken: ${escapeHTML(value.totalTimeInMs.toString())} ms</p>
                    <p>Total Words: ${escapeHTML(value.totalWords.toString())}</p>
                    <table>
                        <tr>
                            <th>Word</th>
                            <th>Count</th>
                        </tr>
        `;
        value.wordCount.forEach(([word, count]) => {
            html += `<tr><td>${escapeHTML(word)}</td><td>${escapeHTML(count.toString())}</td></tr>`;
        });
        html += `</table></div></div>`;
        return html;
    }).join('');

    document.getElementById('results').innerHTML = `<div class="results-grid">${columns}</div>`;
    addToggleEventListeners();
}

function escapeHTML(str) {
    return str.replace(/[&<>"']/g, function(match) {
        return {'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'}[match];
    });
}

function addToggleEventListeners() {
    document.querySelectorAll('.toggle-button').forEach(button => {
        button.addEventListener('click', () => {
            const content = button.nextElementSibling;
            content.style.display = content.style.display === 'none' ? 'block' : 'none';
        });
    });
}

function displayIgnoredWords(ignoredWords) {
    const html = `
        <h3>Ignored Words:</h3>
        <p>${ignoredWords.join(", ")}</p>
    `;
    document.getElementById('ignoredWords').innerHTML = html;
}

function displayError(error) {
    if (error) {
        const html = `
        <h3>Error: </h3>
        <p>${error}</p>
        `;
        document.getElementById('errorSection').innerHTML = html;
    }
}