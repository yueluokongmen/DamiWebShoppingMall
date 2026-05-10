<template>
  <div class="home">
    <div class="site-header">
      <div class="container header-content">
        <h2 class="logo">大米商城</h2>

        <div class="header-search">
          <form class="search-form" @submit.prevent="handleSearch">
            <input type="text" v-model="searchKeyword" class="search-text" placeholder="搜索商品名称">
            <button type="submit" class="search-btn">
              <el-icon>
                <Search />
              </el-icon>
            </button>
          </form>
        </div>

        <div class="right-actions">
          <div class="user-area">
            <template v-if="userStore.userInfo.username">
              <span class="welcome">欢迎，{{ userStore.userInfo.username }}</span>
              <span class="sep">|</span>
              <span class="link" @click="$router.push('/user')">个人中心</span>
              <span class="sep">|</span>
              <span class="link" @click="handleLogout">退出</span>
            </template>
            <template v-else>
              <span class="link" @click="$router.push('/login')">登录</span>
              <span class="sep">|</span>
            </template>
          </div>

          <div class="topbar-cart" @mouseenter="handleCartHover" @mouseleave="cartHover = false">
            <div class="cart-mini" :class="{ 'active': cartHover }" @click="$router.push('/cart')">
              <el-icon class="icon">
                <ShoppingCart />
              </el-icon>
              购物车
              <span class="cart-num">({{ totalCount }})</span>
            </div>
            <div class="cart-menu" v-show="cartHover">
              <div class="loading" v-if="loadingCart">加载中...</div>
              <div class="msg-empty" v-else-if="miniCartList.length === 0">购物车中还没有商品，赶紧选购吧！</div>
              <div class="mini-list-wrapper" v-else>
                <ul class="cart-list">
                  <li v-for="item in miniCartList.slice(0, 5)" :key="item.cartId" class="cart-item">
                    <div class="thumb"><img :src="item.productImage || defaultImg" @error="useDefaultImg"></div>
                    <div class="name" :title="item.productName">{{ item.productName }}</div>
                    <div class="price-box">{{ item.price }}元 × {{ item.cartQuantity }}</div>
                    <el-icon class="del-btn" @click.stop="handleDeleteFromMini(item.cartId)">
                      <Close />
                    </el-icon>
                  </li>
                </ul>
                <div class="cart-total">
                  <div class="info">
                    <div class="count">共 {{ totalCount }} 件商品</div>
                    <div class="price"><span>{{ totalPrice }}</span>元</div>
                  </div>
                  <el-button color="#ff6700" class="btn-go" @click="$router.push('/cart')">去购物车结算</el-button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="home-hero-container container">
      <div class="site-category">
        <ul class="category-list">
          <li v-for="category in categoryList" :key="category.id" class="category-item">

            <a href="javascript:;" class="title">
              <span class="category-icon">
                <component v-if="category.icon" :is="category.icon"
                  style="width: 18px; height: 18px; margin-right: 8px; vertical-align: middle;" />
              </span>

              {{ category.name }}

              <el-icon class="arrow-right">
                <ArrowRight />
              </el-icon>
            </a>

            <div class="children-col" v-if="category.children && category.children.length > 0">
              <div v-for="sub in category.children" :key="sub.id" class="sub-item" @click="searchByFamily(sub.id)">
                <img :src="sub.img" alt="">
                <span>{{ sub.name }}</span>
              </div>
            </div>
          </li>
        </ul>
      </div>

      <div class="site-swiper">
        <el-carousel height="460px" trigger="click" :interval="4000">
          <el-carousel-item v-for="(img, index) in swiperList" :key="index">
            <img :src="img" class="swiper-img" />
          </el-carousel-item>
        </el-carousel>
      </div>
    </div>

    <div class="page-main">
      <div class="container">

        <h2 class="section-title">手机</h2>
        <div class="goods-box">
          <div class="promo-card">
            <img src="/imgs/sidebar1.webp" alt="">
          </div>
          <div class="goods-list">
            <div v-for="item in phoneList.slice(0, 8)" :key="item.productId" class="goods-card" @click="toDetail(item)">
              <div class="img-wrap">
                <img :src="item.images && item.images[0] ? item.images[0] : ''" alt="">
              </div>
              <h3 class="name">{{ item.productName }}</h3>
              <p class="desc">{{ item.subtitle || '旗舰性能，超清影像' }}</p>
              <p class="price">¥ {{ item.spuPrice }}</p>
            </div>
            <div v-if="phoneList.length === 0" class="empty-floor">暂无手机商品</div>
          </div>
        </div>

        <h2 class="section-title">笔记本 平板</h2>
        <div class="goods-box">
          <div class="promo-card">
            <img src="/imgs/sidebar2.webp" alt="">
          </div>
          <div class="goods-list">
            <div v-for="item in laptopList.slice(0, 8)" :key="item.productId" class="goods-card"
              @click="toDetail(item)">
              <div class="img-wrap">
                <img :src="item.images && item.images[0] ? item.images[0] : ''" alt="">
              </div>
              <h3 class="name">{{ item.productName }}</h3>
              <p class="desc">{{ item.subtitle || '生产力工具，轻薄便携' }}</p>
              <p class="price">¥ {{ item.spuPrice }}</p>
            </div>
            <div v-if="laptopList.length === 0" class="empty-floor">暂无笔记本商品</div>
          </div>
        </div>

        <!-- 猜你喜欢模块 -->
        <div class="guess-section" v-if="guessList.length > 0">
          <h2 class="section-title">猜你喜欢</h2>
          <div class="goods-box">
            <div class="goods-list" style="margin-left: 0;">
              <div v-for="item in guessList.slice(0, 8)" :key="item.productId" class="goods-card" @click="toDetail(item)">
                <div class="img-wrap">
                  <img :src="item.images && item.images[0] ? item.images[0] : ''" alt="">
                </div>
                <h3 class="name">{{ item.productName }}</h3>
                <p class="desc">{{ item.subtitle || '为你推荐' }}</p>
                <p class="price">¥ {{ item.spuPrice }}</p>
              </div>
            </div>
          </div>
        </div>

      </div>
    </div>

    <div class="site-footer">
      <div class="container">大米商城 © 2025 Mi Mall Demo</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import request from '../utils/request'
import { useUserStore } from '../stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, ShoppingCart, Close, Search } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

//定义两个楼层的数据列表
const phoneList = ref<any[]>([])
const laptopList = ref<any[]>([])
const guessList = ref<any[]>([])

const defaultImg = '/imgs/default.webp'
const cartHover = ref(false)
const loadingCart = ref(false)
const miniCartList = ref<any[]>([])
const swiperList = [
  '/imgs/lunbotu1.jpg',
  '/imgs/lunbotu2.webp',
  '/imgs/lunbotu3.webp',
]
const categoryList = ref<any[]>([])
const searchKeyword = ref('')

const totalCount = computed(() => miniCartList.value.reduce((sum, item) => sum + item.cartQuantity, 0))
const totalPrice = computed(() => miniCartList.value.reduce((sum, item) => sum + item.price * item.cartQuantity, 0).toFixed(2))

const handleSearch = () => {
  if (!searchKeyword.value.trim()) return
  router.push({ path: '/search', query: { keyword: searchKeyword.value } })
}

const searchByFamily = (familyId: number) => {
  router.push({ path: '/search', query: { familyId: familyId } })
}

const fetchNav = async () => {
  try {
    const res: any = await request.get('/home/nav')
    categoryList.value = res
  } catch (e) { console.error(e) }
}

const fetchFloors = async () => {
  try {
    // 加载手机 (categoryId=1)
    const phoneRes: any = await request.get('/spu/search', { params: { categoryId: 1 } })
    phoneList.value = phoneRes

    // 加载笔记本 (categoryId=2)
    const laptopRes: any = await request.get('/spu/search', { params: { categoryId: 2 } })
    laptopList.value = laptopRes
  } catch (error) { console.error(error) }
}

const fetchMiniCart = async () => {
  if (!userStore.userInfo.userId) return
  loadingCart.value = true
  try {
    const res: any = await request.get(`/cart/list?userId=${userStore.userInfo.userId}`)
    miniCartList.value = res
  } catch (e) { } finally { loadingCart.value = false }
}

const handleCartHover = () => { cartHover.value = true; fetchMiniCart() }
const handleDeleteFromMini = async (cartId: number) => {
  try {
    await request.delete(`/cart/delete/${cartId}`)
    fetchMiniCart()
  } catch (e) { }
}
const fetchGuess = async () => {
  try {
    const res: any = await request.get('/recommend/for-you', {
      params: { userId: userStore.userInfo.userId || undefined }
    })
    guessList.value = res || []
  } catch (e) { console.error(e) }
}

const handleLogout = () => {
  userStore.logout()
  miniCartList.value = []
}
const toDetail = (item: any) => { router.push(`/detail/${item.productId}`) }
const useDefaultImg = (e: Event) => { (e.target as HTMLImageElement).src = defaultImg }

onMounted(() => {
  fetchNav()
  fetchFloors()
  fetchMiniCart()
  fetchGuess()
})
</script>

<style scoped lang="scss">
.container {
  width: 1226px;
  margin: 0 auto;
}

.site-header {
  height: 100px;
  background: #fff;
  border-bottom: 1px solid #e0e0e0;
  position: relative;
  z-index: 20;

  .header-content {
    display: flex;
    align-items: center;
    height: 100%;
  }

  .logo {
    color: #ff6700;
    font-size: 28px;
    margin: 0;
    cursor: pointer;
    margin-right: 50px;
  }

  .header-search {
    width: 300px;
    margin-right: auto;

    .search-form {
      display: flex;
      width: 100%;
      height: 50px;
      border: 1px solid #e0e0e0;
      transition: border-color .2s;

      &:hover {
        border-color: #b0b0b0;
      }

      .search-text {
        border: none;
        width: 248px;
        padding: 0 10px;
        height: 48px;
        outline: none;
        font-size: 14px;
      }

      .search-btn {
        width: 50px;
        height: 48px;
        border: none;
        border-left: 1px solid #e0e0e0;
        background: #fff;
        cursor: pointer;
        transition: all .2s;
        font-size: 18px;
        color: #616161;

        &:hover {
          background: #ff6700;
          color: #fff;
          border-color: #ff6700;
        }
      }
    }
  }

  .right-actions {
    display: flex;
    align-items: center;
  }

  .user-area {
    font-size: 14px;
    color: #757575;
    display: flex;
    align-items: center;

    .link {
      cursor: pointer;
      transition: color .2s;

      &:hover {
        color: #ff6700;
      }
    }

    .sep {
      margin: 0 10px;
      color: #e0e0e0;
    }
  }

  .topbar-cart {
    width: 120px;
    margin-left: 15px;
    position: relative;

    .cart-mini {
      height: 40px;
      line-height: 40px;
      background: #424242;
      text-align: center;
      color: #b0b0b0;
      cursor: pointer;
      transition: all .2s;
      display: flex;
      align-items: center;
      justify-content: center;

      .icon {
        font-size: 20px;
        margin-right: 4px;
      }

      &:hover,
      &.active {
        background: #fff;
        color: #ff6700;
      }
    }

    .cart-menu {
      position: absolute;
      right: 0;
      top: 40px;
      width: 316px;
      background: #fff;
      box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
      z-index: 30;
      color: #424242;

      .loading,
      .msg-empty {
        padding: 40px 0;
        text-align: center;
        font-size: 14px;
      }

      .mini-list-wrapper {
        .cart-list {
          list-style: none;
          padding: 0;
          margin: 0;
          max-height: 400px;
          overflow-y: auto;

          .cart-item {
            display: flex;
            align-items: center;
            padding: 15px 20px;
            border-bottom: 1px solid #e0e0e0;

            &:hover {
              background: #f5f5f5;

              .del-btn {
                opacity: 1;
              }
            }

            .thumb {
              width: 60px;
              height: 60px;
              margin-right: 15px;
              border: 1px solid #eee;

              img {
                width: 100%;
                height: 100%;
                object-fit: cover;
              }
            }

            .name {
              flex: 1;
              font-size: 14px;
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              text-align: left;
            }

            .price-box {
              padding: 0 10px;
              text-align: right;
              font-size: 14px;
              color: #ff6700;
            }

            .del-btn {
              cursor: pointer;
              font-size: 16px;
              opacity: 0;
              transition: opacity .2s;

              &:hover {
                color: #ff6700;
              }
            }
          }
        }

        .cart-total {
          padding: 15px 20px;
          background: #fafafa;
          display: flex;
          justify-content: space-between;
          align-items: center;

          .price {
            color: #ff6700;
            font-size: 20px;
            font-weight: bold;
          }

          .btn-go {
            width: 130px;
          }
        }
      }
    }
  }
}

.home-hero-container {
  position: relative;
  height: 460px;
  margin-bottom: 26px;
  margin-top: 20px;
}

.site-category {
  position: absolute;
  top: 0;
  left: 0;
  width: 234px;
  height: 100%;
  background: rgba(105, 101, 101, 0.6);
  z-index: 10;
  padding: 20px 0;
  box-sizing: border-box;

  ul.category-list {
    list-style-type: none !important;
    padding: 0;
    margin: 0;
  }

  .category-item {
    height: 42px;
    line-height: 42px;

    .title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 0 30px;
      color: #fff;
      font-size: 14px;
      text-decoration: none;
    }

    &:hover {
      background: #ff6700;

      .children-col {
        display: grid;
      }
    }

    .children-col {
      display: none;
      position: absolute;
      left: 234px;
      top: 0;
      height: 460px;
      width: 500px;
      background: #fff;
      border: 1px solid #e0e0e0;
      box-shadow: 0 8px 16px rgba(0, 0, 0, 0.18);
      z-index: 20;
      padding: 20px;
      grid-template-columns: repeat(2, 1fr);
      align-content: flex-start;
      gap: 10px;

      .sub-item {
        display: flex;
        align-items: center;
        cursor: pointer;
        padding: 10px;

        img {
          width: 40px;
          height: 40px;
          margin-right: 12px;
          object-fit: contain;
        }

        span {
          font-size: 14px;
          color: #333;
        }

        &:hover span {
          color: #ff6700;
        }
      }
    }
  }
}

.site-swiper .swiper-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 商品楼层 CSS */
.page-main {
  background: #f5f5f5;
  padding-bottom: 50px;
  padding-top: 20px;

  .section-title {
    font-size: 22px;
    font-weight: 200;
    line-height: 58px;
    color: #333;
  }

  .goods-box {
    display: flex;
    margin-bottom: 30px;

    .promo-card {
      width: 234px;
      height: 614px;
      transition: all .2s linear;
      cursor: pointer;
      margin-right: 14px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
      }
    }

    .goods-list {
      flex: 1;
      display: flex;
      flex-wrap: wrap;
      gap: 14px;

      .goods-card {
        width: 234px;
        height: 300px;
        background: #fff;
        padding: 20px 0;
        text-align: center;
        transition: all .2s linear;
        cursor: pointer;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 15px 30px rgba(0, 0, 0, 0.1);
        }

        .img-wrap {
          width: 160px;
          height: 160px;
          margin: 0 auto 18px;

          img {
            width: 100%;
            height: 100%;
            object-fit: contain;
          }
        }

        .name {
          font-size: 14px;
          font-weight: 400;
          color: #333;
          margin: 0 10px 2px;
          text-overflow: ellipsis;
          white-space: nowrap;
          overflow: hidden;
        }

        .desc {
          font-size: 12px;
          color: #b0b0b0;
          margin: 0 10px 10px;
          text-overflow: ellipsis;
          white-space: nowrap;
          overflow: hidden;
        }

        .price {
          color: #ff6700;
          font-size: 14px;
        }
      }

      .empty-floor {
        width: 100%;
        height: 300px;
        line-height: 300px;
        text-align: center;
        color: #999;
        background: #fff;
      }
    }
  }
}

.site-footer {
  text-align: center;
  padding: 30px 0;
  color: #999;
  background: #fff;
}

.guess-section {
  margin-top: 30px;

  .goods-box {
    display: block;

    .goods-list {
      display: flex;
      flex-wrap: wrap;
      gap: 14px;
    }
  }
}
</style>