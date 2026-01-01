import request from '@/utils/request'

/**
 * Add a product to favorites
 * @param {Number} studentId 
 * @param {Number} productId 
 */
export const addFavorite = (studentId, productId) => {
  return request.post('/favorite/add', { studentId, productId })
}

/**
 * Remove a product from favorites
 * @param {Number} studentId 
 * @param {Number} productId 
 */
export const removeFavorite = (studentId, productId) => {
  return request.delete('/favorite/remove', { 
    params: { studentId, productId } 
  })
}

/**
 * Check if a product is favorited
 * @param {Number} studentId 
 * @param {Number} productId 
 * @returns {Promise<Boolean>}
 */
export const checkFavorite = (studentId, productId) => {
  return request.get('/favorite/check', { 
    params: { studentId, productId } 
  })
}

/**
 * Get paginated list of favorited products
 * @param {Number} studentId 
 * @param {Number} pageNum 
 * @param {Number} pageSize 
 * @returns {Promise<Page>}
 */
export const getFavoriteList = (studentId, pageNum = 1, pageSize = 20) => {
  return request.get('/favorite/list', { 
    params: { studentId, pageNum, pageSize } 
  })
}

/**
 * Get total count of favorites for a student
 * @param {Number} studentId 
 * @returns {Promise<Number>}
 */
export const getFavoriteCount = (studentId) => {
  return request.get('/favorite/count', { 
    params: { studentId } 
  })
}
