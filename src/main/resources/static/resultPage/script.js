document.getElementById('loadAllResults').addEventListener('click', function() {
    fetch('/api/response.json')
        .then(response => response.json())
        .then(data => {
            const sortedResults = sortWordCounts(data.results);
            console.log(sortedResults);
            displayResults2(sortedResults);
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

    /*fetch('/api/wordcount', {
        method: 'POST',
        body: formData
    })
        .then(response => response.json())
        .then(data => {
            displayResults(data);
            //sessionStorage.setItem('analysisResults', JSON.stringify(data.jsonResults));
            sessionStorage.setItem('analysisResults', JSON.stringify(data));
            console.log("index page: " + data.jsonResults);
        })
        .catch(error => {
            console.error('Error processing files:', error);
            document.getElementById('results-display').innerHTML = `<p>Error loading results.</p>`;
        });*/
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
// expired code
function displayResults(results) {
    const keys = Object.keys(results);
    
    const columns = keys.map(key => {
        const value = results[key];
        let html = `
            <div class="result-column">
                <button class="toggle-button">${key}</button>
                <div class="result-content" style="display: none;">
                    <p>Time Taken: ${value.timeInMs} ms</p>
                    <p>Total Words: ${value.totalWords}</p>
                    <table>
                        <tr>
                            <th>Word</th>
                            <th>Count</th>
                        </tr>
        `;
        for (const [word, count] of Object.entries(value.wordCount)) {
            html += `<tr><td>${word}</td><td>${count}</td></tr>`;

        }
        html += `</table></div></div>`;
        return html;
    }).join('');

    document.getElementById('results').innerHTML = `<div class="results-grid">${columns}</div>`;
    addToggleEventListeners();
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

function drawWordCloud(wordCounts) {
    const words = Object.keys(wordCounts).map(key => ({
        text: key,
        size: wordCounts[key] * 10 // 调整字体大小比例因子
    }));

    const color = d3.scaleOrdinal(d3.schemeCategory10); // 使用 D3 的颜色方案

    const layout = d3.layout.cloud()
        .size([800, 600])
        .words(words)
        .padding(5)
        .rotate(0) // 不旋转文字
        .font("Impact")
        .fontSize(d => d.size)
        .on("end", draw);

    layout.start();

    function draw(words) {
        const svg = d3.select("#wordCloud").append("svg")
            .attr("width", layout.size()[0])
            .attr("height", layout.size()[1])
            .append("g")
            .attr("transform", "translate(" + layout.size()[0] / 2 + "," + layout.size()[1] / 2 + ")");

        svg.selectAll("text")
            .data(words)
            .enter().append("text")
            .style("font-size", d => d.size + "px")
            .style("font-family", "Impact")
            .style("fill", (d, i) => color(i)) // 添加颜色
            .attr("text-anchor", "middle")
            .attr("transform", d => "translate(" + [d.x, d.y] + ")rotate(" + d.rotate + ")")
            .text(d => d.text)
            .style("opacity", 0)
            .transition() // 添加简单的淡入效果
            .duration(1000)
            .style("opacity", 1);
    }
}
