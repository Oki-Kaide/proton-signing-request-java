var response = []

if (global.encoder)
    response.push('Global encoder found')

if (encoder)
    response.push('encoder available without global')


response.join('\n')